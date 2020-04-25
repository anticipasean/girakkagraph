package io.github.anticipasean.girakkagraph.protocol.supervisor.worker;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AskPattern;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.command.lookup.shareable.LookUpShareableResultsFound;
import io.github.anticipasean.girakkagraph.protocol.base.command.lookup.shareable.LookUpShareableResultsFoundImpl;
import io.github.anticipasean.girakkagraph.protocol.base.command.lookup.shareable.LookUpShareables;
import io.github.anticipasean.girakkagraph.protocol.base.command.lookup.shareable.ProvideShareableRepositoryImpl;
import io.github.anticipasean.girakkagraph.protocol.base.command.monitoring.IssueTimeout;
import io.github.anticipasean.girakkagraph.protocol.base.command.monitoring.IssueTimeoutImpl;
import io.github.anticipasean.girakkagraph.protocol.base.util.shareable.Shareable;
import io.github.anticipasean.girakkagraph.protocol.base.util.shareable.ShareableCriteria;
import io.github.anticipasean.girakkagraph.protocol.base.util.shareable.ShareableRepository;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.BehaviorCreator;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SpawnedContext;
import io.github.anticipasean.girakkagraph.protocol.base.worker.WorkerActor;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.immutables.criteria.backend.WriteResult;

public class ShareableResolutionWorker extends WorkerActor<LookUpShareables> {

  private final AtomicReference<ShareableRepository> shareableRepositoryHolder;

  protected ShareableResolutionWorker(SpawnedContext<Command, LookUpShareables> spawnedContext) {
    super(spawnedContext);
    this.shareableRepositoryHolder = new AtomicReference<>();
  }

  public static Behavior<Command> create(ActorRef<Command> parentActorRef) {
    return BehaviorCreator.create(
        parentActorRef, LookUpShareables.class, ShareableResolutionWorker::new);
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilderWithDefaultCommandHandlers()
        .onMessage(LookUpShareables.class, this::onLookUpShareables)
        .build();
  }

  private Behavior<Command> onLookUpShareables(LookUpShareables lookUpShareables) {
    context.getLog().info("look_up_shareables received: " + lookUpShareables);
    if (shareableRepositoryNotYetAvailable()) {
      obtainShareableRepositoryFromParent();
    }
    if (lookUpShareables.lookUpShareableQueries().size() == 0) {
      LookUpShareableResultsFound lookUpShareableResultsFound =
          LookUpShareableResultsFoundImpl.builder().commandId(lookUpShareables.commandId()).build();
      lookUpShareables
          .replyTo()
          .ifPresent(
              lookUpShareableResultsFoundActorRef -> {
                lookUpShareableResultsFoundActorRef.tell(lookUpShareableResultsFound);
              });
      return Behaviors.same();
    }
    ShareableCriteria criteria =
        lookUpShareables.lookUpShareableQueries().stream()
            .reduce(
                ShareableCriteria.shareable,
                (crit, query) ->
                    crit.name
                        .is(query.name())
                        .javaClass
                        .is(query.registeredType())
                        .valueContainerType
                        .is(query.valueContainerType()),
                (c1, c2) -> c1.or(c2));
    List<Shareable> shareableList = shareableRepositoryHolder.get().find(criteria).fetch();
    LookUpShareableResultsFound lookUpShareableResultsFound =
        LookUpShareableResultsFoundImpl.builder()
            .commandId(lookUpShareables.commandId())
            .addAllShareablesFound(shareableList)
            .build();
    lookUpShareables
        .replyTo()
        .ifPresent(
            ref -> {
              ref.tell(lookUpShareableResultsFound);
            });
    return Behaviors.same();
  }

  private boolean shareableRepositoryNotYetAvailable() {
    return shareableRepositoryHolder.get() == null;
  }

  private void obtainShareableRepositoryFromParent() {
    context.getLog().info("obtaining shareable repository from parent");
    CompletionStage<ShareableRepository> shareableRepositoryFuture =
        AskPattern.ask(
            parentRef().narrow(),
            ref -> ProvideShareableRepositoryImpl.builder().replyTo(ref).build(),
            Duration.ofSeconds(10),
            context.getSystem().scheduler());
    Optional<ShareableRepository> shareableRepositoryMaybe =
        shareableRepositoryFuture
            .<Optional<ShareableRepository>>handleAsync(
                (shareableRepository, throwable) -> {
                  if (throwable != null) {
                    return Optional.empty();
                  } else {
                    return Optional.of(shareableRepository);
                  }
                })
            .toCompletableFuture()
            .join();
    if (shareableRepositoryMaybe.isPresent()) {
      context.getLog().info("shareable repository received");
      shareableRepositoryHolder.set(shareableRepositoryMaybe.get());
    } else {
      String message =
          "did not receive shareable repository from parent ref within the given timeout limit";
      IllegalStateException illegalStateException = new IllegalStateException(message);
      context.getLog().error(message, illegalStateException);
      IssueTimeout issueTimeout =
          IssueTimeoutImpl.builder()
              .timeOutSetting(Duration.ZERO)
              .errorOccurred(illegalStateException)
              .build();
      selfRef().tell(issueTimeout);
    }
  }

  private void logWriteResults(
      WriteResult updateResultsToExistingRegDep,
      WriteResult insertResultForRegDepYetToBeRegistered) {
    context
        .getLog()
        .info(
            String.format(
                "write_results: [ %s ]",
                Arrays.asList(updateResultsToExistingRegDep, insertResultForRegDepYetToBeRegistered)
                    .stream()
                    .map(
                        writeResult ->
                            String.format(
                                "result:[i: %s, u: %s]",
                                writeResult.insertedCount().orElse(0L),
                                writeResult.updatedCount().orElse(0L)))
                    .collect(Collectors.joining(", "))));
  }
}

/*
 private List<Dependency> getDependenciesMatchingOtherCriteria(
      LookUpDependency lookUpDependency, List<Dependency> dependenciesMatchingInitialCriteria) {
    List<Dependency> dependenciesMatchingOtherCriteria = new ArrayList<>();
    if (lookUpDependency.classTypeIfClass().isPresent()) {
      List<Dependency> dependenciesMatchingClassContainerCriterion =
          getDependenciesMatchingClassContainerCriterion(
              lookUpDependency, dependenciesMatchingInitialCriteria);
      dependenciesMatchingOtherCriteria.addAll(dependenciesMatchingClassContainerCriterion);
    }
    if (lookUpDependency.elementTypeIfList().isPresent()) {
      List<Dependency> dependenciesMatchingListContainerCriterion =
          getDependenciesMatchingListContainerCriterion(
              lookUpDependency, dependenciesMatchingInitialCriteria);
      dependenciesMatchingOtherCriteria.addAll(dependenciesMatchingListContainerCriterion);
    }
    if (lookUpDependency.fullJavaClassNameWithParameterTypesIfKnown().isPresent()) {
      List<Dependency> dependenciesMatchingFullNameCriterion =
          getDependenciesMatchingFullNameCriterion(
              lookUpDependency, dependenciesMatchingInitialCriteria);
      dependenciesMatchingOtherCriteria.addAll(dependenciesMatchingFullNameCriterion);
    }
    return dependenciesMatchingOtherCriteria;
  }

  private List<Dependency> getDependenciesMatchingClassContainerCriterion(
      LookUpDependency lookUpDependency, List<Dependency> dependenciesMatchingInitialCriteria) {
    Optional<Dependency> dependencyFoundMaybe;
    Class<?> valueClassType = lookUpDependency.classTypeIfClass().get();
    return dependenciesMatchingInitialCriteria.stream()
        .filter(
            dependency ->
                dependency
                    .valueContainer()
                    .containerType()
                    .isAssignableFrom(Dependency.ClassContainer.class))
        .filter(
            dependency ->
                getClassValue((Dependency.ClassContainer<?>) dependency.valueContainer())
                    .isAssignableFrom(valueClassType))
        .collect(Collectors.toList());
  }

  private List<Dependency> getDependenciesMatchingFullNameCriterion(
      LookUpDependency lookUpDependency, List<Dependency> dependenciesMatchingInitialCriteria) {
    String fullJavaClassNameWithParams =
        lookUpDependency.fullJavaClassNameWithParameterTypesIfKnown().get();
    return dependenciesMatchingInitialCriteria.stream()
        .filter(
            dependency ->
                dependency
                    .fullJavaClassNameWithParameterizedType()
                    .equals(fullJavaClassNameWithParams))
        .collect(Collectors.toList());
  }

  private List<Dependency> getDependenciesMatchingListContainerCriterion(
      LookUpDependency lookUpDependency, List<Dependency> dependenciesMatchingInitialCriteria) {
    Class<?> elementClass = lookUpDependency.elementTypeIfList().get();
    return dependenciesMatchingInitialCriteria.stream()
        .filter(dependency -> dependency.valueContainer() instanceof Dependency.ListContainer)
        .filter(
            dependency ->
                elementClass.isAssignableFrom(
                    ((Dependency.ListContainer) dependency.valueContainer()).listElementType()))
        .collect(Collectors.toList());
  }
  private WriteResult insertNewRegisteredDependencyEntriesFromCaller(
      Set<RegisteredDependency> registeredDependenciesForCallerYetToBeRegistered) {
    return registeredContainerRepositoryHolder
        .get()
        .insertAll(registeredDependenciesForCallerYetToBeRegistered);
  }

  private boolean registeredDependencyRepositoryNotYetAvailable() {
    return registeredContainerRepositoryHolder.get() == null;
  }
private List<RegisteredDependency> getAllAlreadyRegisteredDependencyEntriesInRepositoryFromList(
      List<RegisteredDependency> allRegisteredDependenciesForCaller) {
    return registeredContainerRepositoryHolder
        .get()
        .find(
            RegisteredDependencyCriteria.registeredDependency.dependency.in(
                allRegisteredDependenciesForCaller.stream()
                    .map(RegisteredDependency::dependency)
                    .collect(Collectors.toSet())))
        .fetch();
  }
  private WriteResult updateAllRegisteredDependencyEntriesSuchThatCallerActorRefAdded(
      Set<RegisteredDependency> alreadyPresentRegDepUpdatedWithCallerActorRef) {
    return registeredContainerRepositoryHolder
        .get()
        .updateAll(alreadyPresentRegDepUpdatedWithCallerActorRef);
  }


  protected Behavior<Command> onRegisterDependencies(RegisterDependencies command) {
    context.getLog().info("register_dependencies: " + command);
    if (registeredDependencyRepositoryNotYetAvailable()) {
      obtainShareableRepositoryFromParent();
    }
    Dependencies dependencies = command.dependencies();
    List<RegisteredDependency> allRegisteredDependenciesForCaller =
        createRegisteredDependencyEntryObjectsForAllDependenciesListedByCaller(
            command, dependencies);
    List<RegisteredDependency> allRegisteredDependenciesAlreadyPresentInRepoFromOtherCalls =
        getAllAlreadyRegisteredDependencyEntriesInRepositoryFromList(
            allRegisteredDependenciesForCaller);
    Set<RegisteredDependency> alreadyPresentRegDepUpdatedWithCallerActorRef =
        generateRegisteredDependencyEntrySetUpdatedWithCallerActorRef(
            command, allRegisteredDependenciesAlreadyPresentInRepoFromOtherCalls);
    WriteResult updateResultsToExistingRegDep =
        updateAllRegisteredDependencyEntriesSuchThatCallerActorRefAdded(
            alreadyPresentRegDepUpdatedWithCallerActorRef);
    Set<RegisteredDependency> registeredDependenciesForCallerYetToBeRegistered =
        getRegisteredDependencySetOfEntriesNotYetPresentInRepository(
            allRegisteredDependenciesForCaller, alreadyPresentRegDepUpdatedWithCallerActorRef);
    WriteResult insertResultForRegDepYetToBeRegistered =
        insertNewRegisteredDependencyEntriesFromCaller(
            registeredDependenciesForCallerYetToBeRegistered);
    logWriteResults(updateResultsToExistingRegDep, insertResultForRegDepYetToBeRegistered);
    replyToCallerWithDependenciesRegisteredCommand(command);
    return Behaviors.same();
  }


  private Set<RegisteredDependency> generateRegisteredDependencyEntrySetUpdatedWithCallerActorRef(
      RegisterDependencies command,
      List<RegisteredDependency> allRegisteredDependenciesAlreadyPresentInRepoFromOtherCalls) {
    return allRegisteredDependenciesAlreadyPresentInRepoFromOtherCalls.stream()
        .map(
            registeredDependency ->
                (RegisteredDependency)
                    RegisteredDependencyImpl.builder()
                        .from(registeredDependency)
                        .addDependentActors(command.replyTo().get())
                        .build())
        .collect(Collectors.toSet());
  }


  private Set<RegisteredDependency> getRegisteredDependencySetOfEntriesNotYetPresentInRepository(
      List<RegisteredDependency> allRegisteredDependenciesForCaller,
      Set<RegisteredDependency> alreadyPresentRegDepUpdatedWithCallerActorRef) {
    Set<RegisteredDependency> registeredDependencySetForCaller =
        allRegisteredDependenciesForCaller.stream().collect(Collectors.toSet());
    return registeredDependencySetForCaller.stream()
        .filter(regDep -> !alreadyPresentRegDepUpdatedWithCallerActorRef.contains(regDep))
        .collect(Collectors.toSet());
  }
  private List<RegisteredDependency>
      createRegisteredDependencyEntryObjectsForAllDependenciesListedByCaller(
          RegisterDependencies command, Dependencies dependencies) {
    return dependencies.dependenciesSet().stream()
        .map(
            dependency ->
                (RegisteredDependency)
                    RegisteredDependencyImpl.builder()
                        .dependency(dependency)
                        .addDependentActors(command.replyTo().get())
                        .build())
        .collect(Collectors.toList());
  }
  private void replyToCallerWithDependenciesRegisteredCommand(RegisterDependencies command) {
    command
        .replyTo()
        .ifPresent(
            ref ->
                ref.tell(
                    DependenciesRegisteredImpl.builder().commandId(command.commandId()).build()));
  }

* */
