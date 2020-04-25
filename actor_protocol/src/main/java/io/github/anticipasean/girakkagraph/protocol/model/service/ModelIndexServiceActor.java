package io.github.anticipasean.girakkagraph.protocol.model.service;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.stream.Materializer;
import akka.stream.javadsl.RunnableGraph;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.command.monitoring.IssueTimeout;
import io.github.anticipasean.girakkagraph.protocol.base.command.monitoring.IssueTimeoutImpl;
import io.github.anticipasean.girakkagraph.protocol.base.command.monitoring.SubordinateTerminated;
import io.github.anticipasean.girakkagraph.protocol.base.command.receptionist.UpdateSubscribedActorRefs;
import io.github.anticipasean.girakkagraph.protocol.base.service.RegistrableActorRouterService;
import io.github.anticipasean.girakkagraph.protocol.base.util.delegation.StallingDelegatePlan;
import io.github.anticipasean.girakkagraph.protocol.base.util.delegation.StallingDelegatePlanImpl;
import io.github.anticipasean.girakkagraph.protocol.base.util.delegation.WaitingDelegateActor;
import io.github.anticipasean.girakkagraph.protocol.base.util.delegation.WaitingDelegatePlan;
import io.github.anticipasean.girakkagraph.protocol.base.util.delegation.WaitingDelegatePlanImpl;
import io.github.anticipasean.girakkagraph.protocol.base.util.dependencies.AkkaSpringDependencyInjectorExtensionId;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.BehaviorCreator;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SpawnedContext;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.ActorRefRegistrations;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.ActorRefSubscriptions;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.RegistrableImpl;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.info.FetchGraphQlSchema;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.info.FetchGraphQlSchemaImpl;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.info.GraphQlSchemaFetched;
import io.github.anticipasean.girakkagraph.protocol.model.command.modelindex.CreateMetaModelDatabase;
import io.github.anticipasean.girakkagraph.protocol.model.command.modelindex.CreateMetaModelDatabaseImpl;
import io.github.anticipasean.girakkagraph.protocol.model.command.modelindex.CreateOperatorDatabase;
import io.github.anticipasean.girakkagraph.protocol.model.command.modelindex.CreateOperatorDatabaseImpl;
import io.github.anticipasean.girakkagraph.protocol.model.command.modelindex.EntityMetaModelIndexed;
import io.github.anticipasean.girakkagraph.protocol.model.command.modelindex.EntityMetaModelIndexedImpl;
import io.github.anticipasean.girakkagraph.protocol.model.command.modelindex.IndexEntityMetaModel;
import io.github.anticipasean.girakkagraph.protocol.model.command.modelindex.IndexEntityMetaModelImpl;
import io.github.anticipasean.girakkagraph.protocol.model.command.modelindex.LookUpModelTypeAndAttributes;
import io.github.anticipasean.girakkagraph.protocol.model.command.modelindex.MetaModelDatabaseCreated;
import io.github.anticipasean.girakkagraph.protocol.model.command.modelindex.MetaModelDatabaseCreatedImpl;
import io.github.anticipasean.girakkagraph.protocol.model.command.modelindex.ModelIndexService;
import io.github.anticipasean.girakkagraph.protocol.model.command.modelindex.OperatorDatabaseCreated;
import io.github.anticipasean.girakkagraph.protocol.model.command.modelindex.OperatorDatabaseCreatedImpl;
import io.github.anticipasean.girakkagraph.protocol.model.command.modelindex.ProvideMetaModelDatabase;
import io.github.anticipasean.girakkagraph.protocol.model.command.modelindex.ProvideOperatorDatabase;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.attribute.PersistableGraphQLAttributeCriteria;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.type.PersistableGraphQLTypeCriteria;
import io.github.anticipasean.girakkagraph.protocol.model.domain.metamodel.MetaModelDatabase;
import io.github.anticipasean.girakkagraph.protocol.model.domain.metamodel.MetaModelDatabaseCreationGraphGenerator;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operator.OperatorDatabase;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operator.jpa.JpaCriteriaOperators;
import io.github.anticipasean.girakkagraph.protocol.model.worker.ModelIndexWorker;
import graphql.schema.GraphQLSchema;
import io.github.anticipasean.girakkagraph.style.naming.EntityNamingConvention;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.Metamodel;

public class ModelIndexServiceActor extends RegistrableActorRouterService<ModelIndexService> {

  private final ActorRef<Command> routerRef;
  private final AtomicReference<ActorRef<FetchGraphQlSchema>> fetchGraphQLSchemaRefSupplier;
  private final AtomicReference<MetaModelDatabase> metaModelDatabaseSupplier;
  private final AtomicReference<GraphQLSchema> graphQLSchemaSupplier;
  private final AtomicReference<OperatorDatabase> operatorDatabaseSupplier;
  private final EntityManager entityManager;
  private final AtomicReference<Consumer<ProvideOperatorDatabase>>
      provideOperatorDbStallingDelegateConsumerHolder;

  protected ModelIndexServiceActor(SpawnedContext<Command, ModelIndexService> spawnedContext) {
    super(spawnedContext);
    routerRef = this.spawnRouter("ModelIndex", ModelIndexWorker::create, Optional.empty());
    fetchGraphQLSchemaRefSupplier = new AtomicReference<>();
    metaModelDatabaseSupplier = new AtomicReference<>();
    graphQLSchemaSupplier = new AtomicReference<>();
    entityManager = obtainEntityManagerFromContext(context);
    operatorDatabaseSupplier = new AtomicReference<>(new OperatorDatabase());
    context
        .getSelf()
        .tell(CreateOperatorDatabaseImpl.builder().replyTo(context.getSelf().narrow()).build());
    provideOperatorDbStallingDelegateConsumerHolder = new AtomicReference<>();
  }

  public static Behavior<Command> create(ActorRef<Command> parentActorRef) {
    return BehaviorCreator.create(
        parentActorRef, ModelIndexService.class, ModelIndexServiceActor::new);
  }

  private EntityManager obtainEntityManagerFromContext(ActorContext<Command> context) {
    EntityManager entityManager =
        AkkaSpringDependencyInjectorExtensionId.getInstance()
            .createExtension(context.getSystem())
            .getEntityManager();
    Objects.requireNonNull(
        entityManager,
        "entity manager obtained from the akka spring dependency injector extension was null");
    return entityManager;
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilderWithDefaultCommandHandlers()
        .onMessage(IndexEntityMetaModel.class, this::onIndexEntityMetaModel)
        .onMessage(EntityMetaModelIndexed.class, this::onEntityMetaModelIndexed)
        .onMessage(CreateMetaModelDatabase.class, this::onCreateMetaModelDatabase)
        .onMessage(MetaModelDatabaseCreated.class, this::onMetaModelDatabaseCreated)
        .onMessage(LookUpModelTypeAndAttributes.class, this::onLookUpModelTypeAndAttributes)
        .onMessage(ProvideMetaModelDatabase.class, this::onProvideMetaModelDatabase)
        .onMessage(CreateOperatorDatabase.class, this::onCreateOperatorDatabase)
        .onMessage(OperatorDatabaseCreated.class, this::onOperatorDatabaseCreated)
        .onMessage(ProvideOperatorDatabase.class, this::onProvideOperatorDatabase)
        .build();
  }

  @Override
  protected Behavior<Command> onIssueTimeout(IssueTimeout command) {
    logger().info("issue_timeout received: " + command);
    if (command.errorOccurred().isPresent()) {
      logger().error("an error occurred and issued a timeout:", command.errorOccurred().get());
      logger()
          .error(
              String.format(
                  "stopping actor: [ name: %s, id: %s, path: %s]",
                  loggingName(), id(), selfRef().path().toSerializationFormat()));
      return Behaviors.stopped();
    }
    return Behaviors.same();
  }

  @Override
  protected Behavior<Command> onSubordinateTerminated(SubordinateTerminated command) {
    logger().info("subordinate_terminated: " + command);
    if (command.errorOccurred().isPresent()) {
      if (command.registeredCommandType().isPresent()
          && ProvideOperatorDatabase.class.isAssignableFrom(
              command.registeredCommandType().get())) {
        logger()
            .info(
                String.format(
                    "removing stalling_delegate_consumer from holder [ holder: %s ]",
                    provideOperatorDbStallingDelegateConsumerHolder.toString()));
        provideOperatorDbStallingDelegateConsumerHolder.set(null);
      }
    }
    return Behaviors.same();
  }

  private Behavior<Command> onOperatorDatabaseCreated(OperatorDatabaseCreated command) {
    logger().info("operator_database_created received: " + command);
    if (command.errorOccurred().isPresent()) {
      logger()
          .error(
              "an error occurred when creating the operator database: ",
              command.errorOccurred().get());
      return Behaviors.stopped();
    }
    return Behaviors.same();
  }

  private Behavior<Command> onProvideOperatorDatabase(ProvideOperatorDatabase command) {
    context.getLog().info("provide_operator_database received: " + command);
    if (command.errorOccurred().isPresent()) {
      logger()
          .error(
              "an error occurred prior to the transmission of this request: "
                  + command.errorOccurred().get());
      return Behaviors.stopped();
    }
    if (operatorDatabaseSupplier.get() == null) {
      return stallRespondingToRequestForOperatorDatabaseUntilReady(command);
    }
    // defensive copy of the operator database so that downstream it may be modified
    // however much the client wants
    // implied dependency on all operator types being immutable
    replyToIfPresent(command, operatorDatabaseSupplier.get().copyOf());
    return Behaviors.same();
  }

  private Behavior<Command> stallRespondingToRequestForOperatorDatabaseUntilReady(
      ProvideOperatorDatabase command) {
    if (provideOperatorDbStallingDelegateConsumerHolder.get() == null) {
      context.getLog().info("stalling until operator database has finished being initialized");
      StallingDelegatePlan<ProvideOperatorDatabase, OperatorDatabase>
          operatorDatabaseStallingDelegatePlan =
              createStallingPlanForProvideOperatorDatabaseRequests();
      provideOperatorDbStallingDelegateConsumerHolder
          .updateAndGet(
              provideOperatorDatabaseStallingDelegateConsumer ->
                  letDelegateStashRequestsAndStallRespondingToThemFollowingPlan(operatorDatabaseStallingDelegatePlan))
          .accept(command);
    } else {
      provideOperatorDbStallingDelegateConsumerHolder.get().accept(command);
    }
    return Behaviors.same();
  }

  private StallingDelegatePlan<ProvideOperatorDatabase, OperatorDatabase>
      createStallingPlanForProvideOperatorDatabaseRequests() {
    Duration timeoutDuration = Duration.ofSeconds(15);
    return StallingDelegatePlanImpl.<ProvideOperatorDatabase, OperatorDatabase>builder()
        .onBehalfOf(selfRef().narrow())
        .stallRespondingToType(ProvideOperatorDatabase.class)
        .matching(provideOperatorDatabase -> !provideOperatorDatabase.errorOccurred().isPresent())
        .untilSupplier(operatorDatabaseSupplier::get)
        .meetsCondition(opDbSupplier -> opDbSupplier.get() != null)
        .checkingNTimes(3)
        .within(timeoutDuration)
        .orElse(
            (provideOperatorDatabases, throwable) -> {
              if (throwable != null) {
                selfRef()
                    .tell(
                        IssueTimeoutImpl.builder()
                            .timeOutSetting(timeoutDuration)
                            .errorOccurred(throwable)
                            .build());
                return;
              }
              selfRef()
                  .tell(
                      IssueTimeoutImpl.builder()
                          .timeOutSetting(timeoutDuration)
                          .errorOccurred(
                              new IllegalStateException(
                                  String.format(
                                      "unable to process requests for the operator database: [ %s ]",
                                      provideOperatorDatabases.stream()
                                          .map(Objects::toString)
                                          .collect(Collectors.joining(",\n")))))
                          .build());
            })
        .build();
  }

  private Behavior<Command> onCreateOperatorDatabase(CreateOperatorDatabase command) {
    context.getLog().info("create_operator_database received: " + command);
    operatorDatabaseSupplier.getAndUpdate(
        JpaCriteriaOperators::populateOperatorDatabaseWithJpaCriteriaOperators);
    replyToIfPresent(
        command,
        OperatorDatabaseCreatedImpl.builder()
            .operatorDatabase(operatorDatabaseSupplier.get())
            .build());
    return Behaviors.same();
  }

  private Behavior<Command> onProvideMetaModelDatabase(ProvideMetaModelDatabase command) {
    context.getLog().info("provide_metamodel_database received: " + command);
    if (metaModelDatabaseSupplier.get() == null) {
      context.getLog().info("metamodel database not yet initialized: stalling for 2 seconds");
      context.scheduleOnce(Duration.ofSeconds(2), selfRef(), command);
      return Behaviors.same();
    }
    replyToIfPresent(command, metaModelDatabaseSupplier.get());
    return Behaviors.same();
  }

  private Behavior<Command> onIndexEntityMetaModel(IndexEntityMetaModel indexEntityMetaModel) {
    context.getLog().info("index_entity_metamodel received: " + indexEntityMetaModel);
    if (metaModelDatabaseSupplier.get() != null) {
      EntityMetaModelIndexed entityMetaModelIndexed =
          EntityMetaModelIndexedImpl.builder()
              .metaModelDatabase(metaModelDatabaseSupplier.get())
              .build();
      replyToIfPresent(indexEntityMetaModel, entityMetaModelIndexed);
      return Behaviors.same();
    }
    ActorRef<FetchGraphQlSchema> fetchGraphQLSchemaActorRef =
        indexEntityMetaModel.fetchGraphQlSchemaActorRef();
    fetchGraphQLSchemaRefSupplier.set(fetchGraphQLSchemaActorRef);
    WaitingDelegatePlan<Command, FetchGraphQlSchema, GraphQlSchemaFetched> waitingDelegatePlan =
        WaitingDelegatePlanImpl.<Command, FetchGraphQlSchema, GraphQlSchemaFetched>builder()
            .onBehalfOf(selfRef())
            .makeRequest(
                graphQLSchemaFetchedActorRef ->
                    FetchGraphQlSchemaImpl.builder().replyTo(graphQLSchemaFetchedActorRef).build())
            .to(fetchGraphQLSchemaActorRef)
            .forResponseOfType(GraphQlSchemaFetched.class)
            .nTimes(3)
            .within(Duration.ofSeconds(20))
            .whenCompleteSendBack(
                (graphQLSchemaFetched, throwable) -> {
                  if (throwable != null) {
                    return CreateMetaModelDatabaseImpl.builder().errorOccurred(throwable).build();
                  } else {
                    return CreateMetaModelDatabaseImpl.builder()
                        .graphQLSchema(graphQLSchemaFetched.graphQLSchema())
                        .build();
                  }
                })
            .build();
    ActorRef<WaitingDelegateActor.Response> delegateRef =
        letDelegateRequestAndWaitForResponseFollowingPlan(waitingDelegatePlan);
    return Behaviors.same();
  }

  private Behavior<Command> onEntityMetaModelIndexed(
      EntityMetaModelIndexed entityMetaModelIndexed) {
    context.getLog().info("entity_metamodel_indexed received: " + entityMetaModelIndexed);
    if (entityMetaModelIndexed.errorOccurred().isPresent()) {
      context
          .getLog()
          .error(
              "an error occurred when indexing the entity metamodel",
              entityMetaModelIndexed.errorOccurred().get());
      return Behaviors.stopped();
    }

    return Behaviors.same();
  }

  private Behavior<Command> onMetaModelDatabaseCreated(
      MetaModelDatabaseCreated metaModelDatabaseCreated) {
    context.getLog().info("metamodel_database_created received: " + metaModelDatabaseCreated);
    if (metaModelDatabaseCreated.errorOccurred().isPresent()) {
      String message =
          "an error occurred when creating the metamodel database used for database schema "
              + "and graphql field definition indexing";
      context.getLog().error(message, metaModelDatabaseCreated.errorOccurred().get());
      return Behaviors.stopped();
    }
    if (metaModelDatabaseCreated.metaModelDatabase().isPresent()) {
      metaModelDatabaseSupplier.set(metaModelDatabaseCreated.metaModelDatabase().get());
    } else {
      context.getLog().error("metamodel database not set on metamodel database created command");
      return Behaviors.stopped();
    }
    testMetaModelDatabase(metaModelDatabaseSupplier.get());
    selfRef().tell(EntityMetaModelIndexedImpl.builder().build());
    return Behaviors.same();
  }

  private void testMetaModelDatabase(MetaModelDatabase metaModelDatabase) {
    if (metaModelDatabase == null) {
      context
          .getLog()
          .error("metaModelDatabase is null", new NullPointerException("metaModelDatabase"));
      return;
    }
    long countOfEntities =
        metaModelDatabase
            .getTypeRepository()
            .find(PersistableGraphQLTypeCriteria.persistableGraphQLType.isEntity.isTrue())
            .count()
            .toCompletableFuture()
            .join();
    context.getLog().info("persistableGraphQLType: isEntity: " + countOfEntities);
    long countOfBasicAttrs =
        metaModelDatabase
            .getAttributeRepository()
            .find(PersistableGraphQLAttributeCriteria.persistableGraphQLAttribute.isBasic.isTrue())
            .count()
            .toCompletableFuture()
            .join();
    context.getLog().info("persistableGraphQLAttribute: isBasic: " + countOfBasicAttrs);
    CompletionStage<List<String>> slugNames =
        metaModelDatabase
            .getTypeRepository()
            .findAll()
            .select(PersistableGraphQLTypeCriteria.persistableGraphQLType.slugName)
            .fetch();
    context
        .getLog()
        .info(
            "slug names of inserted types: [ "
                + slugNames.toCompletableFuture().join().stream()
                    .sorted()
                    .collect(Collectors.joining(",\n"))
                + " ]");
  }

  private Behavior<Command> onCreateMetaModelDatabase(
      CreateMetaModelDatabase createMetaModelDatabase) {
    logger().info("create_metamodel_database received: " + createMetaModelDatabase);
    if (createMetaModelDatabase.errorOccurred().isPresent()) {
      context
          .getLog()
          .error(
              "an error occurred when retrieving the graphql schema: ",
              createMetaModelDatabase.errorOccurred().get());
      return Behaviors.stopped();
    }
    if (metaModelDatabaseSupplier.get() != null) {
      return Behaviors.same();
    }
    if (!createMetaModelDatabase.graphQLSchema().isPresent()) {
      context
          .getLog()
          .error(
              "graphqlschema not available in createMetaModelDatabase command: "
                  + createMetaModelDatabase);
      return Behaviors.stopped();
    }
    GraphQLSchema graphQLSchema = createMetaModelDatabase.graphQLSchema().get();
    graphQLSchemaSupplier.set(graphQLSchema);
    Metamodel metamodel = entityManager.getMetamodel();
    EntityNamingConvention<?> entityNamingConvention = AkkaSpringDependencyInjectorExtensionId.getInstance()
        .createExtension(context.getSystem()).getEntityNamingConvention();
    MetaModelDatabaseCreationGraphGenerator metaModelDatabaseCreationGraphGenerator =
        new MetaModelDatabaseCreationGraphGenerator(entityNamingConvention);
    RunnableGraph<CompletionStage<MetaModelDatabase>> runnableGraph =
        metaModelDatabaseCreationGraphGenerator
            .buildDatabaseCreationGraphUsingJpaMetamodelAndGraphQLSchema(metamodel, graphQLSchema);
    CompletionStage<MetaModelDatabase> metaModelDatabaseCompletionStage = null;
    try {
      context.getLog().info("running creation graph for metamodel database.");
      metaModelDatabaseCompletionStage =
          runnableGraph.run(Materializer.matFromSystem(context.getSystem()));
    } catch (Exception e) {
      context
          .getLog()
          .error(
              "a fatal error occurred during creation of the metamodel database used for lookups of the model's schema",
              e);
    }
    context.pipeToSelf(
        metaModelDatabaseCompletionStage,
        (metaModelDatabase, throwable) -> {
          if (throwable != null) {
            return MetaModelDatabaseCreatedImpl.builder().errorOccurred(throwable).build();
          } else {
            return MetaModelDatabaseCreatedImpl.builder()
                .metaModelDatabase(metaModelDatabase)
                .build();
          }
        });
    return Behaviors.same();
  }

  private boolean shouldTriggerInitialization() {
    return metaModelDatabaseSupplier.get() == null && fetchGraphQLSchemaRefSupplier.get() != null;
  }

  private Behavior<Command> onLookUpModelTypeAndAttributes(
      LookUpModelTypeAndAttributes lookUpModelTypeAndAttributes) {
    context
        .getLog()
        .info("lookup_model_type_and_attributes received: " + lookUpModelTypeAndAttributes);
    routerRef.tell(lookUpModelTypeAndAttributes);
    return Behaviors.same();
  }

  @Override
  public ActorRefRegistrations<ModelIndexService> registrations() {
    return newRegistrationsBuilder()
        .addCommandType(LookUpModelTypeAndAttributes.class)
        .addCommandType(ProvideOperatorDatabase.class)
        .build();
  }

  @Override
  protected ActorRefSubscriptions subscriptions() {
    return newSubscriptionsBuilder().addCommandType(FetchGraphQlSchema.class).build();
  }

  @Override
  protected Behavior<Command> onUpdateSubscribedActorRefs(UpdateSubscribedActorRefs command) {
    context.getLog().info("update_subscribed_actor_refs received: " + command);
    updateActorRefHolderOnUpdateSubscribedActorRefsIfApplicableToRegistrable(
        command, RegistrableImpl.of(FetchGraphQlSchema.class), fetchGraphQLSchemaRefSupplier);
    if (shouldTriggerInitialization()) {
      IndexEntityMetaModel indexEntityMetaModel =
          IndexEntityMetaModelImpl.builder()
              .replyTo(selfRef().narrow())
              .fetchGraphQlSchemaActorRef(fetchGraphQLSchemaRefSupplier.get())
              .build();
      selfRef().tell(indexEntityMetaModel);
    }
    return Behaviors.same();
  }
}
