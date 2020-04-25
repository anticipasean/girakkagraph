package io.github.anticipasean.girakkagraph.protocol.graphql.tracker;

import akka.actor.typed.ActorRef;
import akka.actor.typed.BackoffSupervisorStrategy;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.AskPattern;
import akka.actor.typed.javadsl.Behaviors;
import akka.japi.Pair;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.CommandHandler;
import akka.persistence.typed.javadsl.CommandHandlerBuilder;
import akka.persistence.typed.javadsl.Effect;
import akka.persistence.typed.javadsl.EventHandler;
import akka.persistence.typed.javadsl.EventHandlerBuilder;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.handler.EventSourcedProtocolHandler;
import io.github.anticipasean.girakkagraph.protocol.base.util.dependencies.AkkaSpringDependencyInjectorExtensionId;
import io.github.anticipasean.girakkagraph.protocol.base.util.receptionist.ReceptionistInteractive;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.RegistrableImpl;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.query.QueryGraphQl;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.mapping.MapSelectionSetToModel;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.mapping.MapSelectionSetToModelImpl;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.mapping.SelectionSetMappedToModel;
import io.github.anticipasean.girakkagraph.protocol.graphql.event.query.DataFetchingEnvironmentReceivedImpl;
import io.github.anticipasean.girakkagraph.protocol.graphql.event.query.GraphQLQueryEvent;
import io.github.anticipasean.girakkagraph.protocol.graphql.event.query.HashableModelLookUpCriteriaCalculatedImpl;
import io.github.anticipasean.girakkagraph.protocol.graphql.state.query.GraphQLDataFetchingContextImpl;
import io.github.anticipasean.girakkagraph.protocol.graphql.state.query.GraphQLQueryState;
import io.github.anticipasean.girakkagraph.protocol.graphql.state.query.HashableModelLookUpCriteriaImpl;
import io.github.anticipasean.girakkagraph.protocol.graphql.state.query.NoGraphQLContextImpl;
import io.github.anticipasean.girakkagraph.protocol.model.command.modelindex.LookUpModelTypeAndAttributes;
import io.github.anticipasean.girakkagraph.protocol.model.command.modelindex.LookUpModelTypeAndAttributesImpl;
import io.github.anticipasean.girakkagraph.protocol.model.command.modelindex.ModelIndexService;
import io.github.anticipasean.girakkagraph.protocol.model.command.modelindex.ProvideMetaModelDatabaseImpl;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.BaseModelGraph;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.ModelGraph;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.extrapolator.GraphExtrapolators;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.extrapolator.base.BaseModelGraphExtrapolator;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.extrapolator.query.QueryGraphExtrapolator;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.ModelLookUpCriteriaHashable;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.ModelPath;
import io.github.anticipasean.girakkagraph.protocol.model.domain.metamodel.MetaModelDatabase;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLOutputType;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;

public class GraphQLQueryTracker
    extends EventSourcedProtocolHandler<Command, GraphQLQueryEvent, GraphQLQueryState> {
  private AtomicReference<ActorRef<MapSelectionSetToModel>> selectionMapperActorRefHolder;
  private AtomicReference<ActorRef<LookUpModelTypeAndAttributes>>
      lookUpModelTypeAndAttributesActorRefHolder;

  protected GraphQLQueryTracker(
      ActorContext<Command> context,
      PersistenceId persistenceId,
      Optional<BackoffSupervisorStrategy> onPersistFailure) {
    super(context, persistenceId, onPersistFailure);
  }

  protected GraphQLQueryTracker(ActorContext<Command> context, PersistenceId persistenceId) {
    super(context, persistenceId);
  }

  protected GraphQLQueryTracker(
      ActorContext<Command> context,
      PersistenceId persistenceId,
      BackoffSupervisorStrategy onPersistFailure) {
    super(context, persistenceId, onPersistFailure);
  }

  public GraphQLQueryTracker(
      ActorContext<Command> context,
      PersistenceId persistenceId,
      ActorRef<MapSelectionSetToModel> mapSelectionSetToModelActorRef,
      ActorRef<LookUpModelTypeAndAttributes> lookUpModelTypeAndAttributesActorRef) {
    super(context, persistenceId);
    this.selectionMapperActorRefHolder = new AtomicReference<>(mapSelectionSetToModelActorRef);
    this.lookUpModelTypeAndAttributesActorRefHolder =
        new AtomicReference<>(lookUpModelTypeAndAttributesActorRef);
  }

  public static Behavior<Command> create(
      UUID uuid,
      ActorRef<MapSelectionSetToModel> mapSelectionSetToModelActorRef,
      ActorRef<LookUpModelTypeAndAttributes> lookUpModelTypeAndAttributesActorRef) {
    return Behaviors.setup(
        ctx -> {
          return new GraphQLQueryTracker(
              ctx,
              PersistenceId.apply(UUID.class.getName(), uuid.toString()),
              mapSelectionSetToModelActorRef,
              lookUpModelTypeAndAttributesActorRef);
        });
  }

  @Override
  public GraphQLQueryState emptyState() {
    return NoGraphQLContextImpl.builder().build();
  }

  @Override
  public CommandHandler<Command, GraphQLQueryEvent, GraphQLQueryState> commandHandler() {
    CommandHandlerBuilder<Command, GraphQLQueryEvent, GraphQLQueryState> builder =
        newCommandHandlerBuilder();
    builder
        .forStateType(GraphQLQueryState.NoGraphQLContext.class)
        .onCommand(QueryGraphQl.class, this::onQueryGraphQL);
    builder
        .forStateType(GraphQLQueryState.GraphQLDataFetchingContext.class)
        .onCommand(SelectionSetMappedToModel.class, this::onSelectionSetMappedToModel);
//    builder
//        .forStateType(GraphQLQueryState.HashableModelLookUpCriteria.class)
//        .onCommand(ModelTypeAndAttributesFound.class, this::onModelTypeAndAttributesFound);
//    builder
//        .forStateType(GraphQLQueryState.ModelTypeAndAttributesFound.class)
//        .onCommand(CriteriaQueryFetched.class, this::onCriteriaQueryFetched);

    return builder.build();
  }

  @Override
  public EventHandler<GraphQLQueryState, GraphQLQueryEvent> eventHandler() {
    EventHandlerBuilder<GraphQLQueryState, GraphQLQueryEvent> builder = newEventHandlerBuilder();
    builder
        .forStateType(GraphQLQueryState.NoGraphQLContext.class)
        .onEvent(
            GraphQLQueryEvent.DataFetchingEnvironmentReceived.class,
            this::onDataFetchingEnvironmentReceivedEvent);
    builder
        .forStateType(GraphQLQueryState.GraphQLDataFetchingContext.class)
        .onEvent(
            GraphQLQueryEvent.HashableModelLookUpCriteriaCalculated.class,
            this::onHashableModelLookUpCriteriaCalculatedEvent);
//    builder
//        .forStateType(GraphQLQueryState.HashableModelLookUpCriteria.class)
//        .onEvent(
//            GraphQLQueryEvent.ModelTypeAndAttributesFound.class,
//            this::onModelTypeAndAttributesFoundEvent);
//    builder
//        .forStateType(GraphQLQueryState.ModelTypeAndAttributesFound.class)
//        .onEvent(
//            GraphQLQueryEvent.CriteriaQueryDetermined.class, this::onCriteriaQueryDeterminedEvent);

    return builder.build();
  }

//  private Effect<GraphQLQueryEvent, GraphQLQueryState> onCriteriaQueryFetched(
//      GraphQLQueryState.ModelTypeAndAttributesFound modelTypeAndAttributesFoundState,
//      CriteriaQueryFetched criteriaQueryFetched) {
//    context.getLog().info("criteria_query_fetched received: " + criteriaQueryFetched);
//    return null;
//  }
//
//  private GraphQLQueryState onCriteriaQueryDeterminedEvent(
//      GraphQLQueryState.ModelTypeAndAttributesFound modelTypeAndAttributesFoundState,
//      GraphQLQueryEvent.CriteriaQueryDetermined criteriaQueryDeterminedEvent) {
//    return null;
//  }

//  private GraphQLQueryState onModelTypeAndAttributesFoundEvent(
//      GraphQLQueryState.HashableModelLookUpCriteria hashableModelLookUpCriteriaState,
//      GraphQLQueryEvent.ModelTypeAndAttributesFound modelTypeAndAttributesFoundEvent) {
//    GraphQLQueryState.ModelTypeAndAttributesFound graphQLQueryState =
//        io.github.anticipasean.girakkagraph.protocol.graphql.state.query.ModelTypeAndAttributesFoundImpl
//            .builder()
//            .dataFetchingEnvironment(hashableModelLookUpCriteriaState.dataFetchingEnvironment())
//            .modelLookUpCriteriaHashable(
//                hashableModelLookUpCriteriaState.modelLookUpCriteriaHashable())
//            .modelPathMap(modelTypeAndAttributesFoundEvent.modelPathMap())
//            .build();
//    return graphQLQueryState;
//  }

  private GraphQLQueryState onHashableModelLookUpCriteriaCalculatedEvent(
      GraphQLQueryState.GraphQLDataFetchingContext graphQLDataFetchingContext,
      GraphQLQueryEvent.HashableModelLookUpCriteriaCalculated
          hashableModelLookUpCriteriaCalculated) {
    GraphQLQueryState.HashableModelLookUpCriteria graphQLQueryState =
        HashableModelLookUpCriteriaImpl.builder()
            .dataFetchingEnvironment(graphQLDataFetchingContext.dataFetchingEnvironment())
            .modelLookUpCriteriaHashable(
                hashableModelLookUpCriteriaCalculated.modelLookUpCriteriaHashable())
            .build();
    return graphQLQueryState;
  }

  private GraphQLQueryState onDataFetchingEnvironmentReceivedEvent(
      GraphQLQueryEvent.DataFetchingEnvironmentReceived dataFetchingEnvironmentReceived) {
    GraphQLQueryState.GraphQLDataFetchingContext graphQLQueryState =
        GraphQLDataFetchingContextImpl.builder()
            .dataFetchingEnvironment(dataFetchingEnvironmentReceived.dataFetchingEnvironment())
            .build();
    return graphQLQueryState;
  }

//  private Effect<GraphQLQueryEvent, GraphQLQueryState> onModelTypeAndAttributesFound(
//      GraphQLQueryState.HashableModelLookUpCriteria hashableModelLookUpCriteria,
//      ModelTypeAndAttributesFound modelTypeAndAttributesFound) {
//    context
//        .getLog()
//        .info(
//            "model_type_and_attributes_found received: model_paths: "
//                + (modelTypeAndAttributesFound.modelPathMap().isPresent()
//                    ? modelTypeAndAttributesFound.modelPathMap()
//                        .map(modelPathMap -> modelPathMap.mappedNodes().keySet())
//                        .orElse(new HashSet<>()).stream()
//                        .map(ModelPath::uri)
//                        .map(URI::toString)
//                        .collect(Collectors.joining(", "))
//                    : ""));
//    if (!modelTypeAndAttributesFound.modelPathMap().isPresent()) {
//      // TODO: Implement handling of type not found
//      String message = "model path map not present on modelTypeAndAttributes response";
//      throw new IllegalStateException(message);
//    }
//
//    GraphQLQueryEvent.ModelTypeAndAttributesFound modelTypeAndAttributesFoundEvent =
//        ModelTypeAndAttributesFoundImpl.builder()
//            .modelPathMap(modelTypeAndAttributesFound.modelPathMap().get())
//            .build();
//    FetchCriteriaQuery fetchCriteriaQuery =
//        FetchCriteriaQueryImpl.builder()
//            .modelLookUpCriteriaHashable(hashableModelLookUpCriteria.modelLookUpCriteriaHashable())
//            .modelPathMap(modelTypeAndAttributesFound.modelPathMap().get())
//            .build();
//    Optional<ActorRef<ModelCriteriaService>> modelCriteriaServiceRefMaybe =
//        ReceptionistInteractive.submitBlockingSearchForRegistrableInContextWithTimeout(
//            RegistrableImpl.of(ModelCriteriaService.class), context, Duration.ofSeconds(10));
//    if (modelCriteriaServiceRefMaybe.isPresent()) {
//      return Effect()
//          .persist(modelTypeAndAttributesFoundEvent)
//          .thenRun(() -> modelCriteriaServiceRefMaybe.get().tell(fetchCriteriaQuery));
//    }
//    return Effect().none();
//  }

  private Effect<GraphQLQueryEvent, GraphQLQueryState> onSelectionSetMappedToModel(
      SelectionSetMappedToModel command) {
    context.getLog().info("selection_set_mapped_to_model received: " + command);
    Optional<ModelLookUpCriteriaHashable> modelLookUpCriteriaHashable =
        command.modelLookUpCriteriaHashable();
    if (!modelLookUpCriteriaHashable.isPresent()) {
      // TODO: Implement retry logic to retrieve model lookup hashable
    }
    context.getLog().info("model_lookup_hashable received: " + modelLookUpCriteriaHashable);
    List<ModelPath> pathsFromHashable =
        ModelLookUpCriteriaHashable.pathsFromHashable(modelLookUpCriteriaHashable.get());
    context
        .getLog()
        .info(
            "paths: [ "
                + pathsFromHashable.stream()
                    .map(modelPath -> modelPath.toString())
                    .collect(Collectors.joining(", "))
                + " ]");
    LookUpModelTypeAndAttributes lookUpModelTypeAndAttributes =
        LookUpModelTypeAndAttributesImpl.builder()
            .addAllPaths(pathsFromHashable)
            .replyTo(context.getSelf().narrow())
            .build();
    if (lookUpModelTypeAndAttributesActorRefHolder.get() == null) {
      Optional<ActorRef<LookUpModelTypeAndAttributes>> lookUpModelTypeAndAttributesActorRef =
          ReceptionistInteractive.submitBlockingSearchForRegistrableInContextWithTimeout(
              RegistrableImpl.of(LookUpModelTypeAndAttributes.class),
              context,
              Duration.ofSeconds(8));
      lookUpModelTypeAndAttributesActorRefHolder.set(lookUpModelTypeAndAttributesActorRef.get());
    }
    /** Test code for graph builder */
    //    DelegatePlanImpl.<Command, ProvideMetaModelDatabase,
    // MetaModelDatabase>builder().onBehalfOf(context.getSelf()).makeRequest(metaModelDatabaseActorRef -> ProvideMetaModelDatabaseImpl.builder().replyTo(metaModelDatabaseActorRef).build()).to(lookUpModelTypeAndAttributesActorRefHolder.get()).forResponseOfType(MetaModelDatabase.class).nTimes(2).within(Duration.ofSeconds(10)).;
    CompletionStage<MetaModelDatabase> metaModelDatabaseCompletionStage =
        AskPattern.<ModelIndexService, MetaModelDatabase>ask(
            lookUpModelTypeAndAttributesActorRefHolder.get().unsafeUpcast(),
            ref -> ProvideMetaModelDatabaseImpl.builder().replyTo(ref.narrow()).build(),
            Duration.ofSeconds(5),
            context.getSystem().scheduler());
    BaseModelGraphExtrapolator baseModelGraphExtrapolator =
        GraphExtrapolators
            .newBaseModelGraphExtrapolatorUsingMetaModelDatabaseModelLookUpHashableAndActorContext(
                metaModelDatabaseCompletionStage.toCompletableFuture().join(),
                modelLookUpCriteriaHashable.get(),
                context);
    BaseModelGraph modelGraph = baseModelGraphExtrapolator.extrapolateGraph();
    EntityManager entityManager =
        context.getSystem().hasExtension(AkkaSpringDependencyInjectorExtensionId.getInstance())
            ? context
                .getSystem()
                .extension(AkkaSpringDependencyInjectorExtensionId.getInstance())
                .getEntityManager()
            : AkkaSpringDependencyInjectorExtensionId.getInstance()
                .createExtension(context.getSystem())
                .getEntityManager();
    QueryGraphExtrapolator queryGraphExtrapolator =
        GraphExtrapolators.newQueryGraphExtrapolatorUsingEntityManagerBaseModelGraphAndActorContext(
            entityManager, modelGraph, context);
    ModelGraph queryGraph = queryGraphExtrapolator.extrapolateGraph();
    context.getLog().info("terminating after testing query graph extrapolation");
    context.getSystem().terminate();
    /*
     * End of test code
     */
    if (baseModelGraphExtrapolator != null) {
      return Effect().none();
    }
    GraphQLQueryEvent.HashableModelLookUpCriteriaCalculated event =
        HashableModelLookUpCriteriaCalculatedImpl.builder()
            .modelLookUpCriteriaHashable(modelLookUpCriteriaHashable.get())
            .build();
    return Effect()
        .persist(event)
        .thenRun(
            () ->
                lookUpModelTypeAndAttributesActorRefHolder
                    .get()
                    .tell(lookUpModelTypeAndAttributes));
  }

  private Effect<GraphQLQueryEvent, GraphQLQueryState> onQueryGraphQL(QueryGraphQl command) {
    context.getLog().info("query_graphql received: " + command);
    DataFetchingEnvironment env = command.dataFetchingEnvironment();
    context.getLog().info("field: " + env.getField());
    if (!isFetchingRootField(env)) {
      context.getLog().info("is_fetching_root_field: false");
      context.getLog().info("stopping " + context.getSelf());
      return Effect().stop();
    }
    context.getLog().info("is_fetching_root_field: true");
    String rootFieldName = env.getField().getName();
    GraphQLOutputType rootFieldType = env.getFieldType();
    Map<String, GraphQLOutputType> qualifiedFieldNameToGraphQLOutputTypeMap =
        env.getSelectionSet().getFields().stream()
            .parallel()
            .map(
                selectedField ->
                    Pair.create(
                        selectedField.getQualifiedName(),
                        selectedField.getFieldDefinition().getType()))
            .collect(Collectors.toMap(Pair::first, Pair::second));
    MapSelectionSetToModel mapSelectionSetToModel =
        MapSelectionSetToModelImpl.builder()
            .dataFetchingFieldSelectionSet(env.getSelectionSet())
            .rootField(env.getField())
            .rootFieldType(env.getFieldType())
            .replyTo(context.getSelf().narrow())
            .build();
    GraphQLQueryEvent.DataFetchingEnvironmentReceived dataFetchingEnvironmentReceived =
        DataFetchingEnvironmentReceivedImpl.builder()
            .dataFetchingEnvironment(command.dataFetchingEnvironment())
            .build();
    context.getLog().info("selection_mapper: " + selectionMapperActorRefHolder.get());
    selectionMapperActorRefHolder.get().tell(mapSelectionSetToModel);
    return Effect().persist(dataFetchingEnvironmentReceived);
  }

  private boolean isFetchingRootField(DataFetchingEnvironment env) {
    return env.getSource() == null;
  }
}
