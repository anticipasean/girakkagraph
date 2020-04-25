package io.github.anticipasean.girakkagraph.protocol.graphql.service.query;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AskPattern;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.command.monitoring.IssueTimeout;
import io.github.anticipasean.girakkagraph.protocol.base.command.receptionist.UpdateSubscribedActorRefs;
import io.github.anticipasean.girakkagraph.protocol.base.exception.ProtocolInitializationException;
import io.github.anticipasean.girakkagraph.protocol.base.service.RegistrableActorRouterService;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.BehaviorCreator;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SpawnedContext;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.ActorRefRegistrations;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.ActorRefSubscriptions;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.RegistrableImpl;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.query.DataFetcherProvidedImpl;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.query.ExecuteGraphQlInvocation;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.query.GraphQlInvocationExecuted;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.query.GraphQlInvocationExecutedImpl;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.query.GraphQlQueryProtocol;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.query.ProvideDataFetcher;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.query.ProvideDataFetcherFactory;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.query.QueryGraphQl;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.GraphQlSchemaProtocol;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.info.FetchGraphQlSchemaImpl;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.info.GraphQlSchemaFetched;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.setup.DataFetcherFactoryProvidedImpl;
import io.github.anticipasean.girakkagraph.protocol.graphql.domain.wiring.DefaultActorGraphQLDataFetcherFactory;
import io.github.anticipasean.girakkagraph.protocol.graphql.domain.wiring.DefaultGraphQLDataFetcher;
import io.github.anticipasean.girakkagraph.protocol.graphql.worker.query.QueryGraphQLWorker;
import graphql.ExecutionResult;
import graphql.ExecutionResultImpl;
import graphql.GraphQL;
import graphql.execution.AbortExecutionException;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetcherFactory;
import graphql.schema.GraphQLSchema;
import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;

public class GraphQLQueryServiceActor extends RegistrableActorRouterService<GraphQlQueryProtocol> {
  private final AtomicReference<DataFetcherFactory<CompletionStage<DataFetcherResult<?>>>>
      dataFetcherFactoryHolder;
  private final ActorRef<Command> routerRef;
  private final AtomicReference<GraphQLSchema> schemaRefHolder;
  private final AtomicReference<ActorRef<GraphQlSchemaProtocol>> schemaServiceRefHolder;

  private GraphQLQueryServiceActor(SpawnedContext<Command, GraphQlQueryProtocol> spawnedContext) {
    super(spawnedContext);
    String configPath = "akka.actor.deployment.graphql.query";
    routerRef = spawnRouter("queryGraphQL", QueryGraphQLWorker::create, Optional.of(configPath));
    schemaRefHolder = new AtomicReference<>();
    schemaServiceRefHolder = new AtomicReference<>();
    dataFetcherFactoryHolder = new AtomicReference<>();
  }

  public static Behavior<Command> create(ActorRef<Command> parentActorRef) {
    return BehaviorCreator.create(
        parentActorRef, GraphQlQueryProtocol.class, GraphQLQueryServiceActor::new);
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilderWithDefaultCommandHandlers()
        .onMessage(QueryGraphQl.class, this::onQueryGraphQL)
        .onMessage(ProvideDataFetcher.class, this::onProvideDataFetcher)
        .onMessage(ExecuteGraphQlInvocation.class, this::onExecuteGraphQLInvocation)
        .onMessage(GraphQlInvocationExecuted.class, this::onGraphQLInvocationExecuted)
        .onMessage(ProvideDataFetcherFactory.class, this::onProvideDataFetcherFactory)
        .build();
  }

  private DataFetcherFactory<CompletionStage<DataFetcherResult<?>>> createDataFetcherFactory() {
    return new DefaultActorGraphQLDataFetcherFactory(
        context.getSystem(), new AtomicReference<>(context.getSelf().narrow()));
  }

  private boolean isDataFetcherFactoryAvailable() {
    return dataFetcherFactoryHolder.get() != null;
  }

  private Behavior<Command> onProvideDataFetcherFactory(ProvideDataFetcherFactory command) {
    if (!isDataFetcherFactoryAvailable()) {
      context
          .getLog()
          .info(
              "provide_data_fetcher_factory received: "
                  + command
                  + ", data_fetcher_factory: available: "
                  + false);
      DataFetcherFactory<CompletionStage<DataFetcherResult<?>>> dataFetcherFactory =
          createDataFetcherFactory();
      dataFetcherFactoryHolder.set(dataFetcherFactory);
      context
          .getLog()
          .info(
              "provide_data_fetcher_factory received: "
                  + command
                  + ", data_fetcher_factory: available: "
                  + String.valueOf(dataFetcherFactoryHolder.get() != null));
    }
    replyToIfPresent(
        command,
        DataFetcherFactoryProvidedImpl.builder()
            .commandId(command.commandId())
            .dataFetcherFactory(dataFetcherFactoryHolder.get())
            .build());
    return Behaviors.same();
  }

  private Behavior<Command> onGraphQLInvocationExecuted(GraphQlInvocationExecuted command) {
    context.getLog().info("graphql_invocation_executed received: " + command);
    context.getLog().info("graphql invocation replying to: " + command.replyTo());
    command.replyTo().ifPresent(ref -> ref.tell(command));
    return Behaviors.same();
  }

  private Behavior<Command> onExecuteGraphQLInvocation(ExecuteGraphQlInvocation command) {
    context.getLog().info("execute_graphql_invocation received: " + command);
    context.getLog().info("graphql_schema available: " + schemaRefHolder.get());
    if (schemaRefHolder.get() == null) {
      context.getLog().info("getting graphql schema");
      GraphQLSchema schema = handleSchemaNotAvailableYet();
      context.getLog().info("graphql schema retrieved: " + schema);
      schemaRefHolder.set(schema);
    }
    GraphQL graphQL = null;
    try {
      graphQL = GraphQL.newGraphQL(schemaRefHolder.get()).build();
    } catch (Exception e) {
      context
          .getLog()
          .error(
              "an exception was thrown when generating the graphql execution object: "
                  + e.getClass().getName(),
              e);
      replyToIfPresent(
          command,
          GraphQlInvocationExecutedImpl.builder()
              .commandId(command.commandId())
              .replyTo(command.replyTo())
              .errorOccurred(e)
              .build());
      return Behaviors.same();
    }
    context.getLog().info("created graphql object: " + graphQL);
    context.getLog().info("moving processing off thread");
    moveGraphQLExecutionOffThreadToKeepActorFree(command, graphQL);
    return Behaviors.same();
  }

  private GraphQLSchema handleSchemaNotAvailableYet() {
    context.getLog().info("schema_not_available_yet scenario");
    if (schemaServiceRefHolder.get() == null) {
      throw new ProtocolInitializationException(
          context.getSelf(),
          "schema service ref not available for querying",
          new NoSuchElementException());
    }
    ActorRef<GraphQlSchemaProtocol> schemaServiceRef = schemaServiceRefHolder.get();
    CompletionStage<GraphQlSchemaFetched> schemaRequest =
        AskPattern.ask(
            schemaServiceRef.narrow(),
            ref -> FetchGraphQlSchemaImpl.builder().replyTo(ref).build(),
            Duration.ofSeconds(10),
            context.getSystem().scheduler());
    context.getLog().info("requested graphql schema service ref");
    return schemaRequest
        .handle(
            (graphQLSchemaFetched, throwable) -> {
              if (throwable != null) {
                context
                    .getLog()
                    .error("an error occurred when fetching the graphql schema: ", throwable);
                throw new RuntimeException(throwable);
              }
              context
                  .getLog()
                  .info("graphql_schema received: " + graphQLSchemaFetched.graphQLSchema());
              return graphQLSchemaFetched.graphQLSchema().get();
            })
        .toCompletableFuture()
        .join();
  }

  private void moveGraphQLExecutionOffThreadToKeepActorFree(
      ExecuteGraphQlInvocation command, GraphQL graphQL) {
    try {
      CompletableFuture<ExecutionResult> executionResultCompletableFuture =
          graphQL.executeAsync(command.executionInput());
      context.pipeToSelf(
          executionResultCompletableFuture,
          (executionResult, throwable) -> {
            if (throwable != null) {
              return GraphQlInvocationExecutedImpl.builder()
                  .commandId(command.commandId())
                  .executionResult(
                      ExecutionResultImpl.newExecutionResult()
                          .addError(new AbortExecutionException(throwable))
                          .build())
                  .replyTo(command.replyTo())
                  .errorOccurred(throwable)
                  .build();
            }
            return GraphQlInvocationExecutedImpl.builder()
                .commandId(command.commandId())
                .executionResult(executionResult)
                .replyTo(command.replyTo())
                .build();
          });
    } catch (Exception e) {
      context
          .getLog()
          .error(
              "an error occurred when running the execution result future instance for graphql query: "
                  + command,
              e);
    }
  }

  private Behavior<Command> onProvideDataFetcher(ProvideDataFetcher command) {
    context.getLog().info("provide_data_fetcher received: " + command);
    replyToIfPresent(
        command,
        DataFetcherProvidedImpl.builder()
            .commandId(command.commandId())
            .dataFetcher(createDataFetcher())
            .build());
    return Behaviors.same();
  }

  private DataFetcher<CompletionStage<DataFetcherResult<?>>> createDataFetcher() {
    return new DefaultGraphQLDataFetcher(
        context.getSystem(), new AtomicReference<>(this.context.getSelf().narrow()));
  }

  private Behavior<Command> onQueryGraphQL(QueryGraphQl command) {
    context.getLog().info("received message: {}", command);
    routerRef.tell(command);
    context.getLog().info("message passed to routee");
    return Behaviors.same();
  }

  //  @Override
  //  protected Dependencies dependencies() {
  //    return newDependencyBuilder()
  //        .addDependency(
  //            buildDependency("graphQLWiringFactoryCreator", GraphQLWiringFactoryCreator.class))
  //        .addDependency(buildDependency("graphQLSchema", GraphQLSchema.class))
  //        .build();
  //  }

  @Override
  protected Behavior<Command> onIssueTimeout(IssueTimeout command) {
    context.getLog().info("issue_timeout: " + command);
    if (command.errorOccurred().isPresent()) {
      context.getLog().error("error received on issueTimeout: " + command.errorOccurred().get());
      return Behaviors.stopped();
    }
    return Behaviors.same();
  }

  @Override
  protected Behavior<Command> onUpdateSubscribedActorRefs(UpdateSubscribedActorRefs command) {
    context.getLog().info("receive_actor_ref_updates: " + command);
    if (command.errorOccurred().isPresent()) {
      context
          .getLog()
          .error(
              "update_actor_ref: error occurred when retrieving actor refs" + command,
              command.errorOccurred().get());
      return Behaviors.stopped();
    }
    updateActorRefHolderOnUpdateSubscribedActorRefsIfApplicableToRegistrable(
        command, RegistrableImpl.of(GraphQlSchemaProtocol.class), schemaServiceRefHolder);
    return Behaviors.same();
  }

  @Override
  protected ActorRefSubscriptions subscriptions() {
    return newSubscriptionsBuilder().addCommandType(GraphQlSchemaProtocol.class).build();
  }

  @Override
  public ActorRefRegistrations<GraphQlQueryProtocol> registrations() {
    return newRegistrationsBuilder()
        .addCommandType(QueryGraphQl.class)
        .addCommandType(ProvideDataFetcher.class)
        .addCommandType(ProvideDataFetcherFactory.class)
        .build();
  }
}
/*
  @Override
  protected Dependencies dependencies() {
    return newDependencyBuilder()
        .addDependency(
            buildDependency("graphQLWiringFactoryCreator", GraphQLWiringFactoryCreator.class))
        .addDependency(buildDependency("graphQLSchema", GraphQLSchema.class))
        .build();
  }
    ActorRef<Command> schemaReceiver =
        context.spawn(
            fetchSchemaFromSchemaServiceHoldingOffCommandExecutions(context.getSelf()),
            getId() + "_until_schema_avail");
    context.watchWith(
        schemaReceiver,
        SubordinateTerminatedImpl.builder()
            .registeredCommandType(GraphQLSchemaFetched.class)
            .actorPathOfTerminatedSubordinate(schemaReceiver.path())
            .build());

* return Behaviors.receiveMessage(
        cmd -> {
          if (cmd instanceof SubordinateTerminated) {
            if (((SubordinateTerminated) cmd)
                .registeredCommandType()
                .get()
                .equals(GraphQLSchemaFetched.class)) {
              return Behaviors.same();
            }
          }
          schemaReceiver.tell(cmd);
          return Behaviors.ignore();
        });

         private Behavior<Command> fetchSchemaFromSchemaServiceHoldingOffCommandExecutions(
      ActorRef<Command> currentActorRef) {
    return Behaviors.setup(
        ctx -> {
          List<Command> aggregateCommandsUntilSchemaAvailable = new ArrayList<>();
          return Behaviors.receive(Command.class)
              .onMessage(
                  ExecuteGraphQLInvocation.class,
                  cmd -> {
                    aggregateCommandsUntilSchemaAvailable.add(cmd);
                    return Behaviors.same();
                  })
              .onMessage(
                  QueryGraphQL.class,
                  cmd -> {
                    aggregateCommandsUntilSchemaAvailable.add(cmd);
                    return Behaviors.same();
                  })
              .onMessage(
                  GraphQLSchemaFetched.class,
                  cmd -> {
                    GraphQLSchema schema =
                        cmd.graphQlSchema()
                            .orElseThrow(
                                () ->
                                    new RuntimeException(
                                        "received empty optional for graphql schema from schema service"));
                    schemaRefHolder.set(schema);
                    aggregateCommandsUntilSchemaAvailable.stream()
                        .forEach(command -> currentActorRef.tell(command));
                    return Behaviors.stopped();
                  })
              .build();
        });
  }
* */
/*
  @Override
  protected BiFunction<ActorContext<Command>, StartUp, Behavior<Command>> onStartUpAction() {
    return (context, startup) -> {
      Behavior<Receptionist.Listing> schemaServiceRefSubscription =
          ActorRefSubscription.<GraphQLSchemaCommand>create(
              ServiceKey.create(GraphQLSchemaCommand.class, "schema"), schemaServiceRefHolder);
      ActorRef<Receptionist.Listing> schemaServiceRefSubscriptionRef =
          context.spawn(schemaServiceRefSubscription, "schema_service_ref_subscription");
      return super.onStartUpAction().apply(context, startup);
    };
  }

    @Override
  protected Shareables shareables() {
    return newShareablesBuilder()
        .addShareableSupplier(
            () -> buildShareable("wiringFactory", WiringFactory.class, createWiringFactory()))
        .addShareableSupplier(
            () ->
                buildShareable(
                    "dataFetcherFactory", DataFetcherFactory.class, createDataFetcherFactory()))
        .build();
  }

  private Behavior<Command> retrieveLookUpServiceRefAndRepeatRequestToSelf(
      RequestWiringFactory command) {
    ServiceKey<LookUpShareables> lookUpShareablesServiceKey =
        ServiceKey.create(LookUpShareables.class, "lookUpShareables");
    CompletionStage<Receptionist.Listing> lookUpShareablesListingFuture =
        AskPattern.ask(
            context.getSystem().receptionist(),
            ref -> Receptionist.find(lookUpShareablesServiceKey, ref.narrow()),
            Duration.ofSeconds(10),
            context.getSystem().scheduler());
    lookUpShareablesListingFuture
        .handleAsync(
            (listing, throwable) -> {
              ActorRef<LookUpShareables> lookUpShareablesActorRef = null;
              if (throwable != null) {
                throw new CompletionException(
                    "failure occurred when retrieving look up shareables actor ref", throwable);
              } else {
                Optional<ActorRef<LookUpShareables>> lookUpShareablesActorRefMaybe =
                    listing.getServiceInstances(lookUpShareablesServiceKey).stream().findAny();
                if (lookUpShareablesActorRefMaybe.isPresent()) {
                  lookUpShareablesActorRef = lookUpShareablesActorRefMaybe.get();
                } else {
                  throw new CompletionException(
                      "no lookup shareables actor ref was found in the listing",
                      new NoSuchElementException());
                }
              }
              return lookUpShareablesActorRef;
            },
            context.getExecutionContext())
        .thenAccept(
            lookUpShareablesActorRef -> {
              lookUpShareablesRefHolder.set(lookUpShareablesActorRef);
            });
    return Behaviors.same();
  }

  private Behavior<Command> useLookUpServiceToGetWiringFactoryCreatorAndRepeatRequestToSelf(
      RequestWiringFactory command) {}

else {
      String message =
          "shareable received does not have a value of type GraphQLWiringFactoryCreator";
      NoSuchElementException noSuchElementException = new NoSuchElementException(message);
      context.getLog().error(message, noSuchElementException);
      IssueTimeout issueTimeout =
          IssueTimeoutImpl.builder()
              .timeOutSetting(Duration.ofSeconds(0))
              .errorOccurred(noSuchElementException)
              .build();
      context.getSelf().tell(issueTimeout);
    }
  }

  private GraphQLWiringFactoryCreator lookUpWiringFactoryCreatorAndSaveToHolder() {
    ActorRef<LookUpShareables> lookUpShareablesActorRef = lookUpShareablesRefHolder.get();
    Optional<Dependency> graphQLWiringCreatorDependencyMaybe =
        dependencies().dependenciesSet().stream()
            .filter(dependency -> dependency.type().equals(GraphQLWiringFactoryCreator.class))
            .findAny();
    Dependency dependency =
        graphQLWiringCreatorDependencyMaybe.orElseThrow(
            () ->
                new java.lang.IllegalStateException(
                    "dependency for wiring factory creator not included in dependencies section"));
    LookUpShareables lookUpCmd =
        LookUpShareablesImpl.builder()
            .addLookUpShareableQueries(
                ShareableQueryImpl.builder()
                    .containerType(dependency.valueContainerType())
                    .registeredType(dependency.type())
                    .name(dependency.name())
                    .build())
            .replyTo(context.getSelf().narrow())
            .build();
    CompletionStage<LookUpShareableResultsFound> cmdResponse =
        AskPattern.ask(
            lookUpShareablesActorRef,
            ref ->
                LookUpShareablesImpl.builder()
                    .addLookUpShareableQueries(
                        ShareableQueryImpl.builder()
                            .containerType(dependency.valueContainerType())
                            .registeredType(dependency.type())
                            .name(dependency.name())
                            .build())
                    .replyTo(ref.narrow())
                    .build(),
            Duration.ofSeconds(4),
            context.getSystem().scheduler());
    Optional<Shareable> shareableMaybe =
        cmdResponse
            .handle(
                (response, throwable) -> {
                  if (throwable != null) {
                    throw new CompletionException(
                        "an exception was thrown when retrieving shareable "
                            + "GraphQLWiringFactoryCreator from Look Up Service",
                        throwable);
                  }
                  return response.shareablesFound().stream().findFirst();
                })
            .exceptionally(
                throwable -> {
                  IssueTimeout cmd =
                      IssueTimeoutImpl.builder()
                          .timeOutSetting(Duration.ofSeconds(0))
                          .errorOccurred(throwable)
                          .build();
                  selfRef().tell(cmd);
                  return Optional.empty();
                })
            .toCompletableFuture()
            .join();
    if (shareableMaybe.isPresent()) {
      GraphQLWiringFactoryCreator graphQLWiringFactoryCreator =
          retrieveWiringFactoryCreatorFromShareable(shareableMaybe.get());
      if
    } else {
      IssueTimeout issueTimeoutCmd =
          IssueTimeoutImpl.builder()
              .errorOccurred(
                  new java.lang.IllegalStateException(
                      "retrieval of the graphql wiring factory creator likely timeout"))
              .timeOutSetting(Duration.ofSeconds(0))
              .build();
      context.getSelf().tell(issueTimeoutCmd);
    }
    return null;
  }
private Behavior<Command> startUpPhaseOne(StartUp command) {
    context.getLog().info("startUpMode for: " + context.getSelf());
    final int numberOfTimeoutsAllowed = 3;
    final Duration timeoutInSeconds = Duration.ofSeconds(5);
    final AtomicInteger numberOfTimeoutsOccurred = new AtomicInteger();
    final StartUp startUpCommand = StartUpImpl.copyOf(command);
    final IssueTimeout issueTimeoutCmd =
        (IssueTimeout) IssueTimeoutImpl.builder().timeOutSetting(timeoutInSeconds).build();
    if (isLookUpServiceAvailable() && !isWiringFactoryCreatorAvailable()) {
      lookUpWiringFactoryCreator();
    }
    if (isLookUpServiceAvailable() && isWiringFactoryCreatorAvailable()) {
      WiringFactory wiringFactory = createWiringFactory();
      wiringFactoryHolder.set(wiringFactory);
      startUpCommand
          .replyTo()
          .ifPresent(
              ref ->
                  ref.tell(InitializedImpl.builder().actorPath(context.getSelf().path()).build()));
      return Behaviors.same();
    }
    context.setReceiveTimeout(timeoutInSeconds, issueTimeoutCmd);
    return Behaviors.withStash(
        1000,
        commandStashBuffer -> {
          return Behaviors.receive(Command.class)
              .onMessage(
                  UpdateSubscribedActorRefs.class,
                  cmd -> {
                    onUpdateSubscribedActorRefs(cmd);
                    if (isLookUpServiceAvailable() && !isWiringFactoryCreatorAvailable()) {
                      context.cancelReceiveTimeout();
                      freeBufferedCommandsDuringStartUp(commandStashBuffer);
                      return startUpPhaseOne(command);
                    }
                    return Behaviors.same();
                  })
              .onMessage(
                  LookUpShareableResultsFound.class,
                  cmd -> {
                    onLookUpShareableResultsFound(cmd);
                    if (isLookUpServiceAvailable() && isWiringFactoryCreatorAvailable()) {
                      context.cancelReceiveTimeout();
                      freeBufferedCommandsDuringStartUp(commandStashBuffer);
                      return startUpPhaseOne(command);
                    }
                    return Behaviors.same();
                  })
              .onMessage(
                  IssueTimeout.class,
                  issueTimeout -> {
                    numberOfTimeoutsOccurred.addAndGet(1);
                    if (numberOfTimeoutsOccurred.get() < numberOfTimeoutsAllowed) {
                      context.setReceiveTimeout(timeoutInSeconds, issueTimeoutCmd);
                    } else if (numberOfTimeoutsOccurred.get() >= numberOfTimeoutsAllowed) {
                      context
                          .getLog()
                          .error(
                              new StringBuilder()
                                  .append("startup did not complete even after stalling with: ")
                                  .append(numberOfTimeoutsAllowed)
                                  .append(" ")
                                  .append(timeoutInSeconds.getSeconds())
                                  .append("s timeouts")
                                  .toString());
                      IllegalStateException illegalStateException =
                          new IllegalStateException("startup still not finished");
                      IssueTimeout timeoutCmd =
                          IssueTimeoutImpl.builder()
                              .errorOccurred(illegalStateException)
                              .replyTo(context.getSelf().narrow())
                              .timeOutSetting(
                                  timeoutInSeconds.multipliedBy(numberOfTimeoutsAllowed))
                              .build();
                      context.getSelf().tell(timeoutCmd);
                      return createReceive();
                    }
                    return Behaviors.same();
                  })
              .onMessage(
                  Command.class,
                  cmd ->
                      !(cmd instanceof IssueTimeout)
                          && !(cmd instanceof UpdateSubscribedActorRefs)
                          && !(cmd instanceof LookUpShareableResultsFound),
                  cmd -> {
                    if (!commandStashBuffer.isFull()) {
                      commandStashBuffer.stash(cmd);
                    } else {
                      IllegalStateException illegalStateException =
                          new IllegalStateException(
                              "the buffer for stalling commands until startup has completed is full");
                      IssueTimeout timeoutCmd =
                          IssueTimeoutImpl.builder()
                              .errorOccurred(illegalStateException)
                              .timeOutSetting(timeoutInSeconds)
                              .build();
                      context.getSelf().tell(timeoutCmd);
                      return Behaviors.stopped();
                    }
                    return Behaviors.same();
                  })
              .build();
        });
  }

  private Behavior<Command> startUpPhaseOne(StartUp command) {
    context.getLog().info("startUpMode phase 1 for: " + context.getSelf());
    final int numberOfTimeoutsAllowed = 3;
    final Duration timeoutInSeconds = Duration.ofSeconds(5);
    final AtomicInteger numberOfTimeoutsOccurred = new AtomicInteger();
    final StartUp startUpCommand = StartUpImpl.copyOf(command);
    final IssueTimeout issueTimeoutCmd =
        (IssueTimeout) IssueTimeoutImpl.builder().timeOutSetting(timeoutInSeconds).build();
    if (isLookUpServiceAvailable() && !isWiringFactoryCreatorAvailable()) {
      lookUpWiringFactoryCreator();
    }
    context.setReceiveTimeout(timeoutInSeconds, issueTimeoutCmd);
    return Behaviors.withStash(
        1000,
        commandStashBuffer -> {
          return Behaviors.receive(Command.class)
              .onMessage(
                  UpdateSubscribedActorRefs.class,
                  cmd -> {
                    onUpdateSubscribedActorRefs(cmd);
                    if (isLookUpServiceAvailable() && !isWiringFactoryCreatorAvailable()) {
                      context.cancelReceiveTimeout();
                      freeBufferedCommandsDuringStartUp(commandStashBuffer);
                      return startUpPhaseOne(command);
                    }
                    return Behaviors.same();
                  })
              .onMessage(
                  IssueTimeout.class,
                  issueTimeout -> {
                    numberOfTimeoutsOccurred.addAndGet(1);
                    if (numberOfTimeoutsOccurred.get() < numberOfTimeoutsAllowed) {
                      context.setReceiveTimeout(timeoutInSeconds, issueTimeoutCmd);
                    } else if (numberOfTimeoutsOccurred.get() >= numberOfTimeoutsAllowed) {
                      context
                          .getLog()
                          .error(
                              new StringBuilder()
                                  .append("startup did not complete even after stalling with: ")
                                  .append(numberOfTimeoutsAllowed)
                                  .append(" ")
                                  .append(timeoutInSeconds.getSeconds())
                                  .append("s timeouts")
                                  .toString());
                      IllegalStateException illegalStateException =
                          new IllegalStateException("startup still not finished");
                      IssueTimeout timeoutCmd =
                          IssueTimeoutImpl.builder()
                              .errorOccurred(illegalStateException)
                              .replyTo(context.getSelf().narrow())
                              .timeOutSetting(
                                  timeoutInSeconds.multipliedBy(numberOfTimeoutsAllowed))
                              .build();
                      context.getSelf().tell(timeoutCmd);
                      return createReceive();
                    }
                    return Behaviors.same();
                  })
              .onMessage(
                  Command.class,
                  cmd ->
                      !(cmd instanceof IssueTimeout)
                          && !(cmd instanceof UpdateSubscribedActorRefs)
                          && !(cmd instanceof LookUpShareableResultsFound),
                  cmd -> {
                    if (!commandStashBuffer.isFull()) {
                      commandStashBuffer.stash(cmd);
                    } else {
                      IllegalStateException illegalStateException =
                          new IllegalStateException(
                              "the buffer for stalling commands until startup has completed is full");
                      IssueTimeout timeoutCmd =
                          IssueTimeoutImpl.builder()
                              .errorOccurred(illegalStateException)
                              .timeOutSetting(timeoutInSeconds)
                              .build();
                      context.getSelf().tell(timeoutCmd);
                      return Behaviors.stopped();
                    }
                    return Behaviors.same();
                  })
              .build();
        });
  }

  private Behavior<Command> startUpPhaseTwo(StartUp command) {
    context.getLog().info("startUpMode phase 2 for: " + context.getSelf());
    final int numberOfTimeoutsAllowed = 3;
    final Duration timeoutInSeconds = Duration.ofSeconds(5);
    final AtomicInteger numberOfTimeoutsOccurred = new AtomicInteger();
    final StartUp startUpCommand = StartUpImpl.copyOf(command);
    final IssueTimeout issueTimeoutCmd =
        (IssueTimeout) IssueTimeoutImpl.builder().timeOutSetting(timeoutInSeconds).build();
    context.setReceiveTimeout(timeoutInSeconds, issueTimeoutCmd);
    return Behaviors.withStash(
        1000,
        commandStashBuffer -> {
          return Behaviors.receive(Command.class)
              .onMessage(
                  LookUpShareableResultsFound.class,
                  cmd -> {
                    onLookUpShareableResultsFound(cmd);
                    if (isLookUpServiceAvailable() && isWiringFactoryCreatorAvailable()) {
                      context.cancelReceiveTimeout();
                      freeBufferedCommandsDuringStartUp(commandStashBuffer);
                      return startUpPhaseTwo(command);
                    }
                    return Behaviors.same();
                  })
              .onMessage(
                  IssueTimeout.class,
                  issueTimeout -> {
                    numberOfTimeoutsOccurred.addAndGet(1);
                    if (numberOfTimeoutsOccurred.get() < numberOfTimeoutsAllowed) {
                      context.setReceiveTimeout(timeoutInSeconds, issueTimeoutCmd);
                    } else if (numberOfTimeoutsOccurred.get() >= numberOfTimeoutsAllowed) {
                      context
                          .getLog()
                          .error(
                              new StringBuilder()
                                  .append("startup did not complete even after stalling with: ")
                                  .append(numberOfTimeoutsAllowed)
                                  .append(" ")
                                  .append(timeoutInSeconds.getSeconds())
                                  .append("s timeouts")
                                  .toString());
                      IllegalStateException illegalStateException =
                          new IllegalStateException("startup still not finished");
                      IssueTimeout timeoutCmd =
                          IssueTimeoutImpl.builder()
                              .errorOccurred(illegalStateException)
                              .replyTo(context.getSelf().narrow())
                              .timeOutSetting(
                                  timeoutInSeconds.multipliedBy(numberOfTimeoutsAllowed))
                              .build();
                      context.getSelf().tell(timeoutCmd);
                      return createReceive();
                    }
                    return Behaviors.same();
                  })
              .onMessage(
                  Command.class,
                  cmd ->
                      !(cmd instanceof IssueTimeout)
                          && !(cmd instanceof UpdateSubscribedActorRefs)
                          && !(cmd instanceof LookUpShareableResultsFound),
                  cmd -> {
                    if (!commandStashBuffer.isFull()) {
                      commandStashBuffer.stash(cmd);
                    } else {
                      IllegalStateException illegalStateException =
                          new IllegalStateException(
                              "the buffer for stalling commands until startup has completed is full");
                      IssueTimeout timeoutCmd =
                          IssueTimeoutImpl.builder()
                              .errorOccurred(illegalStateException)
                              .timeOutSetting(timeoutInSeconds)
                              .build();
                      context.getSelf().tell(timeoutCmd);
                      return Behaviors.stopped();
                    }
                    return Behaviors.same();
                  })
              .build();
        });
  }

  private Behavior<Command> startUpPhaseThree(StartUp command) {
    if (isLookUpServiceAvailable() && isWiringFactoryCreatorAvailable()) {
      WiringFactory wiringFactory = createWiringFactory();
      wiringFactoryHolder.set(wiringFactory);
      command
          .replyTo()
          .ifPresent(
              ref ->
                  ref.tell(InitializedImpl.builder().actorPath(context.getSelf().path()).build()));
      return Behaviors.same();
    }
    return Behaviors.stopped();
  }

  private void freeBufferedCommandsDuringStartUp(StashBuffer<Command> stashBuffer) {
    stashBuffer.forEach(context.getSelf()::tell);
  }
private GraphQLWiringFactoryCreator retrieveWiringFactoryCreatorFromShareable(
      Shareable shareable) {
    if (shareable.javaClass().equals(GraphQLWiringFactoryCreator.class)
        && shareable.valueContainerType().isAssignableFrom(DefaultValueContainer.class)) {
      DefaultValueContainer<?> defaultValueContainer =
          (DefaultValueContainer<?>) shareable.valueContainer();
      Object value = getDefaultContainerValue(defaultValueContainer);
      if (value.getClass().isAssignableFrom(GraphQLWiringFactoryCreator.class)) {
        GraphQLWiringFactoryCreator graphQLWiringFactoryCreator =
            (GraphQLWiringFactoryCreator) value;
        return graphQLWiringFactoryCreator;
      }
    }
    return null;
  }

  private Behavior<Command> onStartUp(StartUp command) {
    if (command.errorOccurred().isPresent()) {
      context
          .getLog()
          .error(
              "an error was thrown in the startup of " + selfRef(), command.errorOccurred().get());
      return Behaviors.stopped();
    }
    if (!isLookUpServiceAvailable()) {
      InterceptorPlan<Command, UpdateSubscribedActorRefs, IssueTimeout> interceptorPlan =
          InterceptorPlanImpl.<Command, UpdateSubscribedActorRefs, IssueTimeout>builder()
              .waitForResponseOfType(UpdateSubscribedActorRefs.class)
              .whereResponseMatches(
                  updateSubscribedActorRefs ->
                      updateSubscribedActorRefs
                          .subscriptionWithUpdateReceived()
                          .registrable()
                          .registrableCommandType()
                          .isAssignableFrom(LookUpShareables.class))
              .withTimeout(dur -> IssueTimeoutImpl.builder().timeOutSetting(dur).build())
              .ofType(IssueTimeout.class)
              .nTimes(2)
              .within(Duration.ofSeconds(5))
              .interceptAllOthersOfType(Command.class)
              .bufferingUpToNMessages(100)
              .whenComplete(
                  (updateSubscribedActorRefs, throwable) -> {
                    StartUpImpl.Builder startUpBuilder = StartUpImpl.builder();
                    if (throwable != null) {
                      StartUp startUp = startUpBuilder.errorOccurred(throwable).build();
                      return startUp;
                    } else if (updateSubscribedActorRefs.errorOccurred().isPresent()) {
                      StartUp startUp =
                          startUpBuilder
                              .errorOccurred(updateSubscribedActorRefs.errorOccurred().get())
                              .build();
                      return startUp;
                    } else {
                      StartUp startUp = startUpBuilder.build();
                      return startUp;
                    }
                  })
              .build();
      return executeInterceptorPlan(interceptorPlan, Behaviors.same());
    }

    if (isLookUpServiceAvailable() && !isWiringFactoryCreatorAvailable()) {
      lookUpWiringFactoryCreator();
      InterceptorPlan<Command, LookUpShareableResultsFound, IssueTimeout> interceptorPlan =
          InterceptorPlanImpl.<Command, LookUpShareableResultsFound, IssueTimeout>builder()
              .waitForResponseOfType(LookUpShareableResultsFound.class)
              .whereResponseMatches(
                  lookUpShareableResultsFound ->
                      lookUpShareableResultsFound.shareablesFound().stream()
                          .anyMatch(
                              shareable ->
                                  shareable
                                      .javaClass()
                                      .isAssignableFrom(GraphQLWiringFactoryCreator.class)))
              .withTimeout(duration -> IssueTimeoutImpl.builder().timeOutSetting(duration).build())
              .ofType(IssueTimeout.class)
              .nTimes(2)
              .within(Duration.ofSeconds(5))
              .interceptAllOthersOfType(Command.class)
              .bufferingUpToNMessages(100)
              .whenComplete(
                  (lookUpShareableResultsFound, throwable) -> {
                    if (throwable != null) {
                      return (StartUp) StartUpImpl.builder().errorOccurred(throwable).build();
                    } else {
                      return (StartUp) StartUpImpl.builder().build();
                    }
                  })
              .build();
      return executeInterceptorPlan(interceptorPlan, Behaviors.same());
    }
    if (isLookUpServiceAvailable()
        && isWiringFactoryCreatorAvailable()
        && !isWiringFactoryAvailable()) {
      createWiringFactory();
    }
    if (isLookUpServiceAvailable()
        && isWiringFactoryCreatorAvailable()
        && isWiringFactoryAvailable()) {
      context.getLog().info("all startup conditions met for " + selfRef());
      return Behaviors.same();
    } else {
      context.getLog().error("some component has not initialized properly for " + selfRef());
      return Behaviors.stopped();
    }
  }

  private void lookUpWiringFactoryCreator() {
    ActorRef<LookUpShareables> lookUpShareablesActorRef = lookUpShareablesRefHolder.get();
    Optional<Dependency> graphQLWiringCreatorDependencyMaybe =
        dependencies().dependenciesSet().stream()
            .filter(dependency -> dependency.type().equals(GraphQLWiringFactoryCreator.class))
            .findAny();
    Dependency dependency =
        graphQLWiringCreatorDependencyMaybe.orElseThrow(
            () ->
                new java.lang.IllegalStateException(
                    "dependency for wiring factory creator not included in dependencies section"));
    LookUpShareables lookUpCmd =
        LookUpShareablesImpl.builder()
            .addLookUpShareableQueries(
                ShareableQueryImpl.builder()
                    .containerType(dependency.valueContainerType())
                    .registeredType(dependency.type())
                    .name(dependency.name())
                    .build())
            .replyTo(context.getSelf().narrow())
            .build();
    lookUpShareablesActorRef.tell(lookUpCmd);
  }
  protected Behavior<Command> onLookUpShareableResultsFound(LookUpShareableResultsFound command) {
    if (command.errorOccurred().isPresent()) {
      context
          .getLog()
          .error(
              "an error occurred during processing of request for shareable: ",
              command.errorOccurred().get());
      return Behaviors.stopped();
    }
    Optional<Shareable> shareableMaybe = command.shareablesFound().stream().findFirst();
    if (shareableMaybe.isPresent()
        && shareableMaybe.get().javaClass().isAssignableFrom(GraphQLWiringFactoryCreator.class)) {
      GraphQLWiringFactoryCreator graphQLWiringFactoryCreator =
          retrieveWiringFactoryCreatorFromShareable(shareableMaybe.get());
      if (graphQLWiringFactoryCreator != null) {
        wiringFactoryCreatorHolder.set(graphQLWiringFactoryCreator);
      }
    }
    return Behaviors.same();
  }

    private boolean isLookUpServiceAvailable() {
    return lookUpShareablesRefHolder.get() != null;
  }

  private boolean isWiringFactoryCreatorAvailable() {
    return wiringFactoryCreatorHolder.get() == null;
  }
* */
