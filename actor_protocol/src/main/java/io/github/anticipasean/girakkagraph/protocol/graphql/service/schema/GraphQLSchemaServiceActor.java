package io.github.anticipasean.girakkagraph.protocol.graphql.service.schema;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.DispatcherSelector;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.command.monitoring.IssueTimeoutImpl;
import io.github.anticipasean.girakkagraph.protocol.base.command.monitoring.SubordinateTerminated;
import io.github.anticipasean.girakkagraph.protocol.base.command.receptionist.UpdateSubscribedActorRefs;
import io.github.anticipasean.girakkagraph.protocol.base.exception.ProtocolInitializationException;
import io.github.anticipasean.girakkagraph.protocol.base.service.RegistrableActorRouterService;
import io.github.anticipasean.girakkagraph.protocol.base.util.delegation.StallingDelegatePlan;
import io.github.anticipasean.girakkagraph.protocol.base.util.delegation.StallingDelegatePlanImpl;
import io.github.anticipasean.girakkagraph.protocol.base.util.delegation.WaitingDelegatePlan;
import io.github.anticipasean.girakkagraph.protocol.base.util.delegation.WaitingDelegatePlanImpl;
import io.github.anticipasean.girakkagraph.protocol.base.util.dependencies.AkkaSpringDependencyInjectorExtensionId;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.BehaviorCreator;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SpawnedContext;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.ActorRefRegistrations;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.ActorRefSubscriptions;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.RegistrableImpl;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.GraphQlSchemaProtocol;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.info.FetchGraphQlSchema;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.info.GenerateGraphQlSchemaFile;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.info.GenerateGraphQlSchemaFileImpl;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.info.GraphQlSchemaFetched;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.info.GraphQlSchemaFetchedImpl;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.info.GraphQlSchemaFileGenerated;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.info.GraphQlSchemaFileGeneratedImpl;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.mapping.MapSelectionSetToModel;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.setup.CreateSchemaImpl;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.setup.OperatorDatabaseReceived;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.setup.OperatorDatabaseReceivedImpl;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.setup.ProvideWiringFactory;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.setup.SchemaCreated;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.setup.SetupGraphQlSchema;
import io.github.anticipasean.girakkagraph.protocol.graphql.domain.wiring.DefaultGraphQLWiringFactory;
import io.github.anticipasean.girakkagraph.protocol.graphql.domain.wiring.GraphQLSchemaDirectiveWiringFactory;
import io.github.anticipasean.girakkagraph.protocol.graphql.worker.schema.GraphQLSchemaMappingWorker;
import io.github.anticipasean.girakkagraph.protocol.graphql.worker.schema.GraphQLSchemaSetupWorker;
import io.github.anticipasean.girakkagraph.protocol.model.command.modelindex.ProvideOperatorDatabase;
import io.github.anticipasean.girakkagraph.protocol.model.command.modelindex.ProvideOperatorDatabaseImpl;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operator.OperatorDatabase;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaPrinter;
import graphql.schema.idl.WiringFactory;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GraphQLSchemaServiceActor
    extends RegistrableActorRouterService<GraphQlSchemaProtocol> {
  private final ActorRef<Command> mappingRouterRef;
  private final ActorRef<Command> setupRouterRef;
  private final AtomicReference<WiringFactory> wiringFactoryHolder;
  private final AtomicReference<ActorRef<ProvideOperatorDatabase>> provideOperatorDatabaseRefHolder;
  private final AtomicReference<OperatorDatabase> operatorDatabaseHolder;
  private final AtomicReference<GraphQLSchema> graphQLSchemaHolder;
  private final AtomicReference<Consumer<FetchGraphQlSchema>> stallingDelegateConsumerHolder;

  protected GraphQLSchemaServiceActor(
      SpawnedContext<Command, GraphQlSchemaProtocol> spawnedContext) {
    super(spawnedContext);
    this.graphQLSchemaHolder = new AtomicReference<>();
    setupRouterRef =
        spawnRouter(
            "graphQLSchemaSetup", GraphQLSchemaSetupWorker::create, Optional.of(id()));
    mappingRouterRef =
        spawnRouter("graphQLSchemaMapping", GraphQLSchemaMappingWorker::create, Optional.empty());
    wiringFactoryHolder = new AtomicReference<>();
    provideOperatorDatabaseRefHolder = new AtomicReference<>();
    operatorDatabaseHolder = new AtomicReference<>();
    stallingDelegateConsumerHolder = new AtomicReference<>();
  }

  public static Behavior<Command> create(ActorRef<Command> parentActorRef) {
    return BehaviorCreator.create(
        parentActorRef, GraphQlSchemaProtocol.class, GraphQLSchemaServiceActor::new);
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilderWithDefaultCommandHandlers()
        .onMessage(SchemaCreated.class, this::onSchemaCreated)
        .onMessage(FetchGraphQlSchema.class, this::onFetchGraphQLSchema)
        .onMessage(SubordinateTerminated.class, this::onSubordinateTerminated)
        .onMessage(UpdateSubscribedActorRefs.class, this::onUpdateSubscribedActorRefs)
        .onMessage(MapSelectionSetToModel.class, this::onMapSelectionSetToModel)
        .onMessage(GenerateGraphQlSchemaFile.class, this::onGenerateGraphQLSchemaFile)
        .onMessage(GraphQlSchemaFileGenerated.class, this::onGraphQLSchemaFileGenerated)
        .onMessage(OperatorDatabaseReceived.class, this::onOperatorDatabaseReceived)
        .onMessage(ProvideWiringFactory.class, this::onProvideWiringFactory)
        .build();
  }

  private Behavior<Command> onSchemaCreated(SchemaCreated command) {
    context.getLog().info("schema_created received: " + command);
    if (command.errorOccurred().isPresent()) {
      return returnErrorResponseForInitializationTaskIfRequired(
          command, "an error occurred when generating the executable form of the graphql schema");
    }
    if (!command.graphQLSchemaResult().isPresent()) {
      throw new ProtocolInitializationException(
          selfRef(),
          "no graphql schema was attached to graphql executable schema generated command response",
          new NullPointerException("graphQLSchema"));
    }
    File graphQLSchemaToBeGeneratedFileHandle =
        AkkaSpringDependencyInjectorExtensionId.getInstance()
            .createExtension(context.getSystem())
            .getGraphQLDependencies()
            .graphQLSchemaToBeGeneratedFileHandle();
    graphQLSchemaHolder.set(command.graphQLSchemaResult().get());
    selfRef()
        .tell(
            GenerateGraphQlSchemaFileImpl.builder()
                .graphQLSchemaToBeGeneratedFileHandle(graphQLSchemaToBeGeneratedFileHandle)
                .replyTo(selfRef().narrow())
                .build());
    return Behaviors.same();
  }

  private Behavior<Command> onOperatorDatabaseReceived(OperatorDatabaseReceived command) {
    context.getLog().info("operator_database_received received: " + command);
    if (command.errorOccurred().isPresent()) {
      return handleErrorOccurringWhenRetrievingOperatorDatabase(command);
    }
    if (!command.operatorDatabase().isPresent()) {
      return handleNoOperatorDatabaseSetOnCommand(command);
    }
    operatorDatabaseHolder.set(command.operatorDatabase().get());
    WiringFactory wiringFactory = createWiringFactory();
    wiringFactoryHolder.set(wiringFactory);
    setupRouterRef.tell(
        CreateSchemaImpl.builder()
            .wiringFactory(wiringFactoryHolder.get())
            .replyTo(selfRef().narrow())
            .build());
    return Behaviors.same();
  }

  private Behavior<Command> handleErrorOccurringWhenRetrievingOperatorDatabase(
      OperatorDatabaseReceived command) {
    Function<OperatorDatabaseReceived, String> errorMessageFunc =
        cmd ->
            String.format(
                "an error occurred when retrieving the operator database"
                    + " necessary for graphql schema wiring: %s ",
                cmd.errorOccurred().get().getClass().getSimpleName());
    context.getLog().error(errorMessageFunc.apply(command), command.errorOccurred().get());
    return Behaviors.stopped();
  }

  private Behavior<Command> handleNoOperatorDatabaseSetOnCommand(OperatorDatabaseReceived command) {
    Function<OperatorDatabaseReceived, String> errorMessageFunc =
        cmd ->
            String.format(
                "the command did not provide the operator database"
                    + " necessary for graphql schema wiring: %s ",
                cmd);
    IllegalStateException exception = new IllegalStateException(errorMessageFunc.apply(command));
    context.getLog().error(errorMessageFunc.apply(command), exception);
    return Behaviors.stopped();
  }

  private WiringFactory createWiringFactory() {
    try {
      OperatorDatabase operatorDatabase = operatorDatabaseHolder.get();
      GraphQLSchemaDirectiveWiringFactory graphQLSchemaDirectiveWiringFactory =
          GraphQLSchemaDirectiveWiringFactory.newInstanceUsingOperatorDatabase(operatorDatabase);
      WiringFactory wiringFactory =
          DefaultGraphQLWiringFactory
              .getInstanceUsingActorSystemAndGraphQlSchemaDirectiveWiringFactory(
                  context.getSystem(), graphQLSchemaDirectiveWiringFactory);
      return wiringFactory;
    } catch (Exception e) {
      return handleWiringFactoryCreationFailure(e);
    }
  }

  private WiringFactory handleWiringFactoryCreationFailure(Exception e) {
    String message =
        "an error occurred when creating the wiring factory for the graphql query service";
    context.getLog().error(message, e);
    throw new ProtocolInitializationException(selfRef(), message, e);
  }

  private Behavior<Command> onProvideWiringFactory(ProvideWiringFactory command) {
    context.getLog().info("provide_wiring_factory received: " + command);
    if (!isWiringFactoryAvailable()) {
      context
          .getLog()
          .error("provide_wiring_factory called but wiring factory is not yet available");
      return Behaviors.stopped();
    }
    replyToIfPresent(command, wiringFactoryHolder.get());
    return Behaviors.same();
  }

  private boolean isWiringFactoryAvailable() {
    return wiringFactoryHolder.get() != null;
  }

  private Behavior<Command> onGraphQLSchemaFileGenerated(
      GraphQlSchemaFileGenerated graphQLSchemaFileGenerated) {
    context.getLog().info("graphql_schema_file_generated received: " + graphQLSchemaFileGenerated);
    if (graphQLSchemaFileGenerated.errorOccurred().isPresent()) {
      context
          .getLog()
          .error(
              "an error occurred during graphql schema file resource generation.",
              graphQLSchemaFileGenerated.errorOccurred().get());
    }
    return Behaviors.same();
  }

  private Behavior<Command> onGenerateGraphQLSchemaFile(
      GenerateGraphQlSchemaFile generateGraphQLSchemaFile) {
    context.getLog().info("generate_schema_file received: " + generateGraphQLSchemaFile);
    CompletionStage<File> fileFuture =
        CompletableFuture.supplyAsync(
            () ->
                outputLatestAnnotationGeneratedSchemaToResourceFile(
                    generateGraphQLSchemaFile.graphQLSchemaToBeGeneratedFileHandle()),
            context.getSystem().dispatchers().lookup(DispatcherSelector.blocking()));
    context.pipeToSelf(
        fileFuture,
        (file, throwable) -> {
          if (throwable != null) {
            GraphQlSchemaFileGenerated graphQLSchemaFileGenerated =
                GraphQlSchemaFileGeneratedImpl.builder()
                    .graphQLSchemaToBeGeneratedFileHandle(
                        generateGraphQLSchemaFile.graphQLSchemaToBeGeneratedFileHandle())
                    .errorOccurred(throwable)
                    .build();
            return graphQLSchemaFileGenerated;
          } else {
            GraphQlSchemaFileGenerated graphQLSchemaFileGenerated =
                GraphQlSchemaFileGeneratedImpl.builder()
                    .graphQLSchemaToBeGeneratedFileHandle(file)
                    .build();
            return graphQLSchemaFileGenerated;
          }
        });
    return Behaviors.same();
  }

  private Behavior<Command> onMapSelectionSetToModel(MapSelectionSetToModel command) {
    context.getLog().info("map_selection_set_to_model received: " + command);
    mappingRouterRef.tell(command);
    return Behaviors.same();
  }

  private Behavior<Command> onFetchGraphQLSchema(FetchGraphQlSchema command) {
    context.getLog().info("fetch_graphql_schema received: " + command);
    if (graphQLSchemaHolder.get() == null) {
      return stallUntilSchemaIsAvailable(command);
    }
    GraphQlSchemaFetchedImpl.Builder replyBuilder =
        GraphQlSchemaFetchedImpl.builder().commandId(command.commandId());
    try {
      if (graphQLSchemaHolder.get() == null) {
        throw new ProtocolInitializationException(
            selfRef(),
            "graphql schema should have been set before getting to this step",
            new IllegalStateException("graphql schema not already set"));
      }
      replyBuilder.graphQLSchema(graphQLSchemaHolder.get());
    } catch (IllegalStateException e) {
      context
          .getLog()
          .error("graphql schema did not get properly generated. stopping schema service", e);
      GraphQlSchemaFetched graphQLSchemaFetched = replyBuilder.errorOccurred(e).build();
      replyToIfPresent(command, graphQLSchemaFetched);
      return Behaviors.stopped();
    }
    GraphQlSchemaFetched fetchedReply = replyBuilder.build();
    replyToIfPresent(command, fetchedReply);
    return Behaviors.same();
  }

  private Behavior<Command> stallUntilSchemaIsAvailable(FetchGraphQlSchema command) {
    if (stallingDelegateConsumerHolder.get() == null) {
      context
          .getLog()
          .info(
              "stalling responding to fetch_graphql_schema commands until schema has finished being generated");
      StallingDelegatePlan<FetchGraphQlSchema, GraphQLSchema>
          stallingDelegatePlanForFetchGraphQLRequests =
              createStallingDelegatePlanForFetchGraphQLRequests();
      stallingDelegateConsumerHolder
          .updateAndGet(
              fetchGraphQlSchemaConsumer ->
                  letDelegateStashRequestsAndStallRespondingToThemFollowingPlan(stallingDelegatePlanForFetchGraphQLRequests))
          .accept(command);
    } else {
      stallingDelegateConsumerHolder.get().accept(command);
    }
    return Behaviors.same();
  }

  private StallingDelegatePlan<FetchGraphQlSchema, GraphQLSchema>
      createStallingDelegatePlanForFetchGraphQLRequests() {
    Duration timeoutDuration = Duration.ofSeconds(15);
    return StallingDelegatePlanImpl.<FetchGraphQlSchema, GraphQLSchema>builder()
        .onBehalfOf(selfRef().narrow())
        .stallRespondingToType(FetchGraphQlSchema.class)
        .matching(fetchGraphQlSchema -> !fetchGraphQlSchema.errorOccurred().isPresent())
        .untilSupplier(graphQLSchemaHolder::get)
        .meetsCondition(graphQLSchemaSupplier -> graphQLSchemaSupplier.get() != null)
        .checkingNTimes(3)
        .within(timeoutDuration)
        .orElse(
            (fetchGraphQlSchemas, throwable) -> {
              if (throwable != null) {
                selfRef()
                    .tell(
                        IssueTimeoutImpl.builder()
                            .timeOutSetting(timeoutDuration)
                            .errorOccurred(
                                new IllegalStateException(
                                    String.format(
                                        "unable to respond to requests [ %d of %s ] because of error:",
                                        fetchGraphQlSchemas.size(),
                                        FetchGraphQlSchema.class.getSimpleName()),
                                    throwable))
                            .build());
                return;
              }
              selfRef()
                  .tell(
                      IssueTimeoutImpl.builder()
                          .errorOccurred(
                              new IllegalStateException(
                                  String.format(
                                      "uanble to respond to requests [ %s ]: timeout waiting for graphql "
                                          + "schema generation to complete",
                                      fetchGraphQlSchemas.stream()
                                          .map(Objects::toString)
                                          .collect(Collectors.joining(",\n")))))
                          .timeOutSetting(timeoutDuration)
                          .build());
            })
        .build();
  }

  private Behavior<Command> returnErrorResponseForInitializationTaskIfRequired(
      SetupGraphQlSchema command, String message) {
    context.getLog().error(message, command.errorOccurred().get());
    return Behaviors.stopped();
  }

  private File outputLatestAnnotationGeneratedSchemaToResourceFile(
      File graphQLSchemaToBeGeneratedFileHandle) {
    GraphQLSchema graphQLSchema = graphQLSchemaHolder.get();
    SchemaPrinter.Options options =
        SchemaPrinter.Options.defaultOptions()
            .includeDirectives(true)
            .includeExtendedScalarTypes(true)
            .includeSchemaDefinition(true)
            .includeScalarTypes(true);
    SchemaPrinter schemaPrinter = new SchemaPrinter(options);
    String printedSchema = schemaPrinter.print(graphQLSchema);
    try {
      if (!graphQLSchemaToBeGeneratedFileHandle.exists()) {
        boolean successfullyCreatedNewSchemaFile =
            graphQLSchemaToBeGeneratedFileHandle.createNewFile();
        if (!successfullyCreatedNewSchemaFile) {
          throw new IOException(
              "the named file appears to already exist but the handle is reporting it doesn't");
        }
      }
      context
          .getLog()
          .info(
              String.format(
                  "outputting schema to file: [path: %s, len: %d character(s)]",
                  graphQLSchemaToBeGeneratedFileHandle.getPath(),
                  printedSchema == null ? 0 : printedSchema.length()));
      if (printedSchema != null) {
        Files.write(
            graphQLSchemaToBeGeneratedFileHandle.toPath(),
            printedSchema.getBytes(StandardCharsets.UTF_8),
            StandardOpenOption.WRITE,
            StandardOpenOption.TRUNCATE_EXISTING);
      }
    } catch (IOException e) {
      context.getLog().error("unable to output generated graphql schema to resource file", e);
    }
    return graphQLSchemaToBeGeneratedFileHandle;
  }

  @Override
  protected Behavior<Command> onUpdateSubscribedActorRefs(UpdateSubscribedActorRefs command) {
    if (command.errorOccurred().isPresent()) {
      context
          .getLog()
          .error(
              "an error occurred when updating the subscribed actor refs for " + selfRef(),
              command.errorOccurred().get());
      return Behaviors.stopped();
    }
    updateActorRefHolderOnUpdateSubscribedActorRefsIfApplicableToRegistrable(
        command,
        RegistrableImpl.of(ProvideOperatorDatabase.class),
        provideOperatorDatabaseRefHolder);
    if (provideOperatorDatabaseRefHolder.get() != null && operatorDatabaseHolder.get() == null) {
      WaitingDelegatePlan<Command, ProvideOperatorDatabase, OperatorDatabase> plan =
          WaitingDelegatePlanImpl.<Command, ProvideOperatorDatabase, OperatorDatabase>builder()
              .onBehalfOf(selfRef())
              .makeRequest(
                  operatorDatabaseActorRef ->
                      ProvideOperatorDatabaseImpl.builder()
                          .replyTo(operatorDatabaseActorRef)
                          .build())
              .to(provideOperatorDatabaseRefHolder.get())
              .forResponseOfType(OperatorDatabase.class)
              .nTimes(3)
              .within(Duration.ofSeconds(15))
              .whenCompleteSendBack(
                  (operatorDatabase, throwable) -> {
                    OperatorDatabaseReceivedImpl.Builder builder =
                        OperatorDatabaseReceivedImpl.builder();
                    if (throwable != null) {
                      return builder.errorOccurred(throwable).build();
                    }
                    return builder.operatorDatabase(operatorDatabase).build();
                  })
              .build();
      letDelegateRequestAndWaitForResponseFollowingPlan(plan);
    }

    return Behaviors.same();
  }

  @Override
  public ActorRefRegistrations<GraphQlSchemaProtocol> registrations() {
    return newRegistrationsBuilder()
        .addCommandType(FetchGraphQlSchema.class)
        .addCommandType(MapSelectionSetToModel.class)
        .addCommandType(ProvideWiringFactory.class)
        .build();
  }

  @Override
  protected ActorRefSubscriptions subscriptions() {
    return newSubscriptionsBuilder().addCommandType(ProvideOperatorDatabase.class).build();
  }
}
