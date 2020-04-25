package io.github.anticipasean.girakkagraph.protocol.supervisor.service.lookup;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.command.lookup.shareable.LookUpShareables;
import io.github.anticipasean.girakkagraph.protocol.base.command.lookup.shareable.ProvideShareableRepository;
import io.github.anticipasean.girakkagraph.protocol.base.service.RegistrableActorRouterService;
import io.github.anticipasean.girakkagraph.protocol.base.util.shareable.ShareableRepository;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.BehaviorCreator;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SpawnedContext;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.ActorRefRegistrations;
import io.github.anticipasean.girakkagraph.protocol.supervisor.command.lookup.InitializeShareableRepositoryImpl;
import io.github.anticipasean.girakkagraph.protocol.supervisor.worker.ShareableResolutionWorker;
import java.time.Duration;
import java.util.Optional;
import org.immutables.criteria.inmemory.InMemoryBackend;

public class LookUpShareableServiceActor extends RegistrableActorRouterService<LookUpShareables> {
  private final ActorRef<Command> routerRef;
  private final ShareableRepository shareableRepository;
  private final Behavior<Command> initialized = createReceive();

  protected LookUpShareableServiceActor(SpawnedContext<Command, LookUpShareables> spawnedContext) {
    super(spawnedContext);
    routerRef =
        this.spawnRouter("LookUpShareables", ShareableResolutionWorker::create, Optional.empty());
    shareableRepository = new ShareableRepository(new InMemoryBackend());
    selfRef().tell(InitializeShareableRepositoryImpl.builder().build());
  }

  public static Behavior<Command> create(ActorRef<Command> parentActorRef) {
    return BehaviorCreator.create(
        parentActorRef, LookUpShareables.class, LookUpShareableServiceActor::new);
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilderWithDefaultCommandHandlers()
        .onMessage(LookUpShareables.class, this::onLookUpShareables)
//        .onMessage(InitializeShareableRepository.class, this::onInitializeShareableRepository)
        .onMessage(ProvideShareableRepository.class, this::onProvideShareableRepository)
        .build();
  }

  private Behavior<Command> onProvideShareableRepository(
      ProvideShareableRepository provideShareableRepository) {
    context.getLog().info("provide_shareable_repository received: " + provideShareableRepository);
    if (!isAlive(initialized)) {
      context
          .getLog()
          .info("not yet fully initialized: delaying providing of shareable repository");
      context.scheduleOnce(Duration.ofSeconds(1), selfRef(), provideShareableRepository);
      return Behaviors.same();
    }
    provideShareableRepository
        .replyTo()
        .ifPresent(
            shareableRepositoryActorRef -> {
              shareableRepositoryActorRef.tell(shareableRepository);
            });
    return Behaviors.same();
  }

//  private Behavior<Command> onInitializeShareableRepository(InitializeShareableRepository command) {
//    context.getLog().info("initialize_shareable_repository received: " + command);
//    return loadDependenciesAvailableInExtension();
//  }

  private Behavior<Command> onLookUpShareables(LookUpShareables lookUpShareables) {
    context.getLog().info("look_up_shareables received: " + lookUpShareables);
    routerRef.tell(lookUpShareables);
    return Behaviors.same();
  }

//  @SuppressWarnings("unchecked")
//  private Behavior<Command> loadDependenciesAvailableInExtension() {
//    AkkaSpringDependencyInjector dependencyInjectorExtension =
//        AkkaSpringDependencyInjectorExtensionId.getInstance().createExtension(context.getSystem());
//    List<Shareable> dependencies =
//        Stream.of(
//                buildShareable(
//                    "graphQLQueryObjectClass",
//                    (Class<Object>)
//                        dependencyInjectorExtension.getGraphQLDependencies().graphQLQueryClass()),
//                buildShareable(
//                    "entityManager",
//                    EntityManager.class,
//                    dependencyInjectorExtension.getEntityManager()),
//                buildShareable(
//                    "graphQLDependencies",
//                    GraphQLDependencies.class,
//                    dependencyInjectorExtension.getGraphQLDependencies()))
//            .collect(Collectors.toList());
//    shareableRepository.insertAll(dependencies);
//    return initialized;
//  }

  @Override
  public ActorRefRegistrations<LookUpShareables> registrations() {
    return newRegistrationsBuilder().build();
  }
}

/*
  @Override
  protected BiFunction<ActorContext<Command>, StartUp, Behavior<Command>> onStartUpAction() {
    return (context, startup) -> {
      context
          .getSelf()
          .tell(InitializeShareableRepositoryImpl.builder().commandId(startup.commandId()).build());
      onStartUpBehaviorInContextWithSimpleInitializedReply().apply(context, startup);
      context.setLoggerName(LookUpShareableServiceActor.class);
      return Behaviors.same();
    };
  }

    private Behavior<Command> provideRepositoriesToWorker(LookUpShareables lookUpShareables) {
    lookUpShareables
        .replyTo()
        .ifPresent(
            ref -> {
              ref.tell(
                  LookUpShareableResultsFoundImpl.builder()
                      .commandId(lookUpShareables.commandId())
                      .addShareablesFound(
                          buildShareable(
                              "shareableRepository",
                              ShareableRepository.class,
                              shareableRepository))
                      //                          buildShareable(
                      //                              "registeredDependencyRepository",
                      //                              RegisteredDependencyRepository.class,
                      //                              registeredDependencyRepository))
                      .build());
            });
    return Behaviors.same();
  }
  private boolean isRequestForShareableRepository(LookUpShareables lookUpShareables) {
    return lookUpShareables.lookUpShareableQueries().stream()
        .findFirst()
        .map(shareableQuery -> shareableQuery.name().equals("shareableRepository"))
        .isPresent();
  }

* */
