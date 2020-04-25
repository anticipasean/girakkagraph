package io.github.anticipasean.girakkagraph.protocol.model.domain.graph.extrapolator.query;

import akka.NotUsed;
import akka.actor.typed.javadsl.ActorContext;
import akka.japi.Pair;
import akka.japi.function.Function;
import akka.japi.function.Function2;
import akka.stream.ClosedShape;
import akka.stream.Materializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.GraphDSL;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.model.domain.context.graph.QueryGraphOperatorProcessingContext;
import io.github.anticipasean.girakkagraph.protocol.model.domain.context.graph.QueryGraphOperatorProcessingContextImpl;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.ModelEdge;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.ModelGraph;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.ModelGraphComponent;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.ModelVertex;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.QueryModelGraph;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.QueryModelGraphImpl;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.QueryModelGraphImpl.Builder;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.ArgumentEdge;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.UnprocessedArgumentEdge;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.RootFromVertex;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.processor.query.CriteriaBasicAttributeVertexArgumentEdgeProcessor;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.processor.query.CriteriaJoinEdgeProcessor;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.processor.query.CriteriaSelectionEdgeProcessor;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.processor.query.CriteriaTypeVertexArgumentEdgeProcessor;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.processor.query.JpaCriteriaQueryProcessors;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.vertex.BasicAttributeVertex;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.vertex.TypeVertex;
import io.github.anticipasean.girakkagraph.typematcher.TypeMatcher;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class QueryGraphExtrapolatorImpl implements QueryGraphExtrapolator {

  private final Logger logger;
  private final EntityManager entityManager;
  private final ModelGraph baseModelGraph;
  private final ActorContext<Command> actorContext;
  private final CriteriaJoinEdgeProcessor joinProcessor;
  private final CriteriaSelectionEdgeProcessor selectionProcessor;
  private final CriteriaBasicAttributeVertexArgumentEdgeProcessor
      basicAttributeVertexArgumentEdgeProcessor;
  private final CriteriaTypeVertexArgumentEdgeProcessor typeVertexArgumentEdgeProcessor;

  public QueryGraphExtrapolatorImpl(
      EntityManager entityManager, ModelGraph baseModelGraph, ActorContext<Command> context) {
    this.entityManager = entityManager;
    this.baseModelGraph = baseModelGraph;
    this.actorContext = context;
    this.logger = LoggerFactory.getLogger(QueryGraphExtrapolatorImpl.class);
    this.joinProcessor = JpaCriteriaQueryProcessors.joinEdgeProcessor();
    this.selectionProcessor = JpaCriteriaQueryProcessors.selectionEdgeProcessor();
    this.basicAttributeVertexArgumentEdgeProcessor =
        JpaCriteriaQueryProcessors.basicAttributeArgumentEdgeProcessor();
    this.typeVertexArgumentEdgeProcessor = JpaCriteriaQueryProcessors.typeArgumentEdgeProcessor();
  }

  @Override
  public QueryModelGraph extrapolateGraph() {
    Source<ModelGraphComponent, NotUsed> modelGraphComponentSource =
        Source.fromIterator(baseModelGraph::traverseBreadthFirst);
    Sink<QueryGraphOperatorProcessingContext, CompletionStage<QueryModelGraph>>
        queryModelGraphSink = takeUpdatedContextsAndCombineIntoQueryModelGraph();
    RunnableGraph<CompletionStage<QueryModelGraph>> queryModelRunnableGraph =
        RunnableGraph.fromGraph(
            GraphDSL.create(
                modelGraphComponentSource,
                queryModelGraphSink,
                Keep.right(),
                (builder, source, sink) -> {
                  builder
                      .from(source)
                      .via(builder.add(provideAndUpdateContextPerModelGraphComponentInFlow()))
                      .to(sink);
                  return ClosedShape.getInstance();
                }));
    return queryModelRunnableGraph
        .run(Materializer.createMaterializer(actorContext))
        .toCompletableFuture()
        .join();
  }

  private Sink<QueryGraphOperatorProcessingContext, CompletionStage<QueryModelGraph>>
      takeUpdatedContextsAndCombineIntoQueryModelGraph() {
    return Sink
        .<Pair<Builder, Optional<QueryGraphOperatorProcessingContext>>,
            QueryGraphOperatorProcessingContext>
            fold(
                Pair.create(QueryModelGraphImpl.builder(), Optional.empty()),
                combineLatestContextWithQueryModelGraphBuilderContextPair())
        .mapMaterializedValue(
            builderContextMaybePairFuture ->
                builderContextMaybePairFuture.thenApply(createQueryModelGraphUsingFinalPair()));
  }

  private java.util.function.Function<
          Pair<Builder, Optional<QueryGraphOperatorProcessingContext>>, QueryModelGraph>
      createQueryModelGraphUsingFinalPair() {
    return builderOptionalPair -> {
      if (builderOptionalPair.second().isPresent()) {
        Builder queryModelGraphBuilder = builderOptionalPair.first();
        QueryGraphOperatorProcessingContext queryGraphOperatorProcessingContext =
            builderOptionalPair.second().get();
        return queryModelGraphBuilder
            .tupleCriteriaQuery(queryGraphOperatorProcessingContext.criteriaQuery())
            .sqlComponentToEdgeMappings(
                queryGraphOperatorProcessingContext.queryCriteriaEdgesProcessingMap())
            .criteriaBuilder(queryGraphOperatorProcessingContext.criteriaBuilder())
            .build();
      }
      throw new IllegalStateException(
          "no context was provided to update the final parts of the query model builder: "
              + "criteria query, criteria builder, and the sql component mapping");
    };
  }

  private Function2<
          Pair<Builder, Optional<QueryGraphOperatorProcessingContext>>,
          QueryGraphOperatorProcessingContext,
          Pair<Builder, Optional<QueryGraphOperatorProcessingContext>>>
      combineLatestContextWithQueryModelGraphBuilderContextPair() {
    return (builderContextMaybePair, queryGraphOperatorProcessingContext) -> {
      queryGraphOperatorProcessingContext
          .currentRoundComponents()
          .forEach(
              modelGraphComponent -> {
                TypeMatcher.whenTypeOf(modelGraphComponent)
                    .is(ModelEdge.class)
                    .then(builderContextMaybePair.first()::addEdge)
                    .is(RootFromVertex.class)
                    .then(
                        rootFromVertex ->
                            builderContextMaybePair
                                .first()
                                .putVertex(rootFromVertex.vertexPath(), rootFromVertex)
                                .addEdge(rootFromVertex)
                                .rootFromVertexIfAvail(rootFromVertex))
                    .is(ModelVertex.class)
                    .then(
                        modelVertex ->
                            builderContextMaybePair
                                .first()
                                .putVertex(modelVertex.vertexPath(), modelVertex))
                    .orElseThrow(
                        modelGraphComponentNotHandledExceptionSupplier(modelGraphComponent));
              });
      return Pair.create(
          builderContextMaybePair.first(), Optional.of(queryGraphOperatorProcessingContext));
    };
  }

  private Supplier<IllegalStateException> modelGraphComponentNotHandledExceptionSupplier(
      ModelGraphComponent modelGraphComponent) {
    return () ->
        new IllegalStateException(
            String.format(
                "the model graph component received [ %s ] has not been properly "
                    + "mapped to a consumer method on the query model graph builder",
                modelGraphComponent));
  }

  private Function<QueryGraphOperatorProcessingContext, QueryGraphOperatorProcessingContext>
      logSomeOfTheLastContextUpdates() {
    return context -> {
      logger.info(
          "context_received: "
              + context.currentRoundComponents().stream()
                  .map(
                      modelGraphComponent ->
                          "processed_component interfaces: [ "
                              + Arrays.stream(modelGraphComponent.getClass().getInterfaces())
                                  .map(Class::getName)
                                  .collect(Collectors.joining(", "))
                              + " ]")
                  .collect(Collectors.joining("; ")));
      if (context.currentRoundComponents().stream()
          .filter(modelGraphComponent -> modelGraphComponent instanceof ArgumentEdge)
          .map(modelGraphComponent -> (ArgumentEdge) modelGraphComponent)
          .anyMatch(argumentEdge -> argumentEdge.childPath().depth() >= 3)) {
        String mapContent =
            context.queryCriteriaEdgesProcessingMap().entrySet().stream()
                .map(
                    sqlConcurrentNavigableMapEntry ->
                        String.join(
                                ": ",
                                sqlConcurrentNavigableMapEntry.getKey().name(),
                                sqlConcurrentNavigableMapEntry.getValue().entrySet().stream()
                                    .map(
                                        entry ->
                                            new StringBuilder("[ ")
                                                .append(entry.getKey())
                                                .append(" : ")
                                                .append(
                                                    Arrays.stream(
                                                            entry
                                                                .getValue()
                                                                .getClass()
                                                                .getInterfaces())
                                                        .map(cls -> cls.getSimpleName())
                                                        .collect(Collectors.joining(",\n\t\t")))
                                                .append(" ]")
                                                .toString())
                                    .collect(Collectors.joining(",\n\t")))
                            + "\n\t")
                .collect(Collectors.joining("\n\t"));
        logger.info("query_criteria_edges_processing_map: " + mapContent);
      }
      return context;
    };
  }

  @Override
  public ModelGraph baseModelGraph() {
    return baseModelGraph;
  }

  @Override
  public EntityManager entityManager() {
    return entityManager;
  }

  private Flow<ModelGraphComponent, QueryGraphOperatorProcessingContext, NotUsed>
      provideAndUpdateContextPerModelGraphComponentInFlow() {
    return Flow.of(ModelGraphComponent.class)
        .scan(
            QueryGraphOperatorProcessingContextImpl.builder()
                .baseModelGraph(baseModelGraph())
                .criteriaBuilder(entityManager().getCriteriaBuilder())
                .criteriaQuery(entityManager.getCriteriaBuilder().createTupleQuery())
                .queryCriteriaEdgesProcessingMap(new QueryCriteriaEdgesProcessingMap())
                .build(),
            this::processModelGraphComponentInQueryGraphProcessingContext)
        .wireTap(queryGraphProcCtx -> logSomeOfTheLastContextUpdates().apply(queryGraphProcCtx));
  }

  private QueryGraphOperatorProcessingContext
      processModelGraphComponentInQueryGraphProcessingContext(
          QueryGraphOperatorProcessingContext queryGraphProcessingContext,
          ModelGraphComponent modelGraphComponent) {
    return TypeMatcher.whenTypeOf(modelGraphComponent)
        .is(TypeVertex.class)
        .thenApply(
            typeVertex ->
                joinProcessor.updateContextAccordingToComponent(
                    queryGraphProcessingContext, typeVertex))
        .is(BasicAttributeVertex.class)
        .thenApply(
            basicAttributeVertex ->
                selectionProcessor.updateContextAccordingToComponent(
                    queryGraphProcessingContext, basicAttributeVertex))
        .is(
            UnprocessedArgumentEdge.class,
            isUnprocessedArgumentEdgeInQueryGraphContextConnectedToVertexType(
                queryGraphProcessingContext, TypeVertex.class))
        .thenApply(
            unprocessedArgumentEdge ->
                typeVertexArgumentEdgeProcessor.updateContextAccordingToComponent(
                    queryGraphProcessingContext, unprocessedArgumentEdge))
        .is(
            UnprocessedArgumentEdge.class,
            isUnprocessedArgumentEdgeInQueryGraphContextConnectedToVertexType(
                queryGraphProcessingContext, BasicAttributeVertex.class))
        .thenApply(
            unprocessedArgumentEdge ->
                basicAttributeVertexArgumentEdgeProcessor.updateContextAccordingToComponent(
                    queryGraphProcessingContext, unprocessedArgumentEdge))
        .orElse(unhandledModelGraphComponentTypeExceptionSupplier());
    // TODO: Directive Edge

  }

  private java.util.function.Function<ModelGraphComponent, QueryGraphOperatorProcessingContext>
      unhandledModelGraphComponentTypeExceptionSupplier() {
    return modelGraphComp -> {
      throw new IllegalStateException(
          String.format(
              "model graph component [ %s ] has not been handled in the processing of the query graph.",
              modelGraphComp));
    };
  }

  private Predicate<UnprocessedArgumentEdge>
      isUnprocessedArgumentEdgeInQueryGraphContextConnectedToVertexType(
          QueryGraphOperatorProcessingContext queryGraphProcessingContext,
          Class<? extends ModelVertex> vertexType) {
    return unprocessedArgumentEdge ->
        Optional.ofNullable(
                queryGraphProcessingContext
                    .baseModelGraph()
                    .vertices()
                    .get(unprocessedArgumentEdge.parentPath()))
            .filter(
                modelVertex ->
                    Objects.requireNonNull(vertexType, "vertexType")
                        .isAssignableFrom(modelVertex.getClass()))
            .isPresent();
  }
}
