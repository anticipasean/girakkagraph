package io.github.anticipasean.girakkagraph.protocol.model.domain.graph.extrapolator.base;

import akka.NotUsed;
import akka.actor.typed.javadsl.ActorContext;
import akka.japi.Pair;
import akka.stream.ClosedShape;
import akka.stream.Graph;
import akka.stream.Outlet;
import akka.stream.UniformFanInShape;
import akka.stream.UniformFanOutShape;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.GraphDSL;
import akka.stream.javadsl.Merge;
import akka.stream.javadsl.Partition;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.BaseModelGraph;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.BaseModelGraphImpl;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.ModelEdge;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.ModelGraph;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.ModelGraphComponent;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.ModelVertex;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.UnprocessedArgumentEdgeImpl;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.vertex.BaseRootVertexImpl;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.vertex.BasicAttributeVertex;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.vertex.BasicAttributeVertexImpl;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.vertex.JunctionVertex;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.vertex.JunctionVertexImpl;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.vertex.RootVertex;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.vertex.TypeVertex;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.ModelLookUpCriteriaHashable;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.ModelPath;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.ModelPathImpl;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.attribute.PersistableGraphQLAttribute;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.attribute.PersistableGraphQLAttributeCriteria;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.type.PersistableGraphQLType;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.type.PersistableGraphQLTypeCriteria;
import io.github.anticipasean.girakkagraph.protocol.model.domain.metamodel.MetaModelDatabase;
import io.github.anticipasean.girakkagraph.typematcher.TypeMatcher;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class BaseModelGraphExtrapolatorImpl implements BaseModelGraphExtrapolator {
  private static final ModelVertex SENTINEL_VERTEX =
      new ModelVertex() {
        @Override
        public ModelPath vertexPath() {
          return ModelPathImpl.builder().build();
        }
      };
  private final Logger logger;
  private final MetaModelDatabase metaModelDatabase;
  //  private final Map<String, Set<Operator>> callNameOperatorMap;
  private final ModelLookUpCriteriaHashable modelLookUpCriteriaHashable;
  private final ActorContext<Command> context;

  public BaseModelGraphExtrapolatorImpl(
      MetaModelDatabase metaModelDatabase,
      ModelLookUpCriteriaHashable modelLookUpCriteriaHashable,
      ActorContext<Command> context) {
    this.modelLookUpCriteriaHashable = modelLookUpCriteriaHashable;
    this.context = context;
    this.logger = LoggerFactory.getLogger(BaseModelGraphExtrapolatorImpl.class);
    this.metaModelDatabase = metaModelDatabase;
    //    this.callNameOperatorMap = Operator.callNameToOperatorSet();
  }

  private static boolean isUnprocessedComponentArgumentEdge(
      UnprocessedModelGraphComponent unprocessedModelGraphComponent) {
    if (unprocessedModelGraphComponent
        .segmentName()
        .equals(unprocessedModelGraphComponent.path().lastSegment())) {
      return false;
    }
    if (!unprocessedModelGraphComponent
        .segmentName()
        .equals(ModelLookUpCriteriaHashable.ComponentType.ARGUMENT.key)) {
      return false;
    }
    return unprocessedModelGraphComponent
        .jsonValue()
        .getValueType()
        .equals(ModelLookUpCriteriaHashable.ComponentType.ARGUMENT.jsonValueType);
  }

  private static boolean isUnprocessedComponentDirectiveEdge(
      UnprocessedModelGraphComponent unprocessedModelGraphComponent) {
    if (unprocessedModelGraphComponent
        .segmentName()
        .equals(unprocessedModelGraphComponent.path().lastSegment())) {
      return false;
    }
    if (!unprocessedModelGraphComponent
        .segmentName()
        .equals(ModelLookUpCriteriaHashable.ComponentType.DIRECTIVE.key)) {
      return false;
    }
    return unprocessedModelGraphComponent
        .jsonValue()
        .getValueType()
        .equals(ModelLookUpCriteriaHashable.ComponentType.DIRECTIVE.jsonValueType);
  }

  @Override
  public BaseModelGraph extrapolateGraph() {
    return buildBaseModelGraphFromModelLookUpCriteriaHashableInActorContext(
        modelLookUpCriteriaHashable, context);
  }

  private BaseModelGraph buildBaseModelGraphFromModelLookUpCriteriaHashableInActorContext(
      ModelLookUpCriteriaHashable modelLookUpCriteriaHashable, ActorContext<Command> context) {
    logger.info(
        "build_model_graph_from_model_lookup_criteria_hashable: " + modelLookUpCriteriaHashable);
    JsonObject pathsJsonObject = modelLookUpCriteriaHashable.lookUpHashable();
    // obtain root path segment entry from hashable
    UnprocessedModelGraphComponent rootModelGraphComponent =
        extractRootUnprocessedModelGraphComponentFromPathsJsonObject(pathsJsonObject);
    Source<UnprocessedModelGraphComponent, NotUsed> pathsJsonSource =
        createJsonPathsSourceFromUnprocessedRootModelGraphComponent(rootModelGraphComponent);
    Sink<ModelGraphComponent, CompletionStage<BaseModelGraph>> modelGraphComponentsGatheringSink =
        gatherAllProcessedModelGraphComponentsIntoGraph();
    Graph<ClosedShape, CompletionStage<BaseModelGraph>> graph =
        buildModelGraphGenerationStreamGraphFromJsonPathsSourceAndModelGraphSink(
            pathsJsonSource, modelGraphComponentsGatheringSink);
    CompletionStage<BaseModelGraph> modelGraphFuture =
        RunnableGraph.fromGraph(graph).run(context.getSystem());
    BaseModelGraph modelGraph = null;
    try {
      modelGraph = modelGraphFuture.toCompletableFuture().join();
    } catch (Exception e) {
      logger.error(
          String.format(
              "an error occurred when building the base model graph for model lookup hashable [ %s ]",
              modelLookUpCriteriaHashable.lookUpHashable()),
          e);
      throw e;
    }
    logInitialModelGraph(modelGraph);
    return modelGraph;
  }

  private UnprocessedModelGraphComponent
      extractRootUnprocessedModelGraphComponentFromPathsJsonObject(JsonObject pathsJsonObject) {
    Map.Entry<String, JsonValue> rootEntry = null;
    try {
      if (pathsJsonObject.keySet().size() == 0) {
        Supplier<String> messageSupplier =
            () -> String.format("the model paths key [ %s ] must have a root", pathsJsonObject);
        throw new IllegalArgumentException(messageSupplier.get());
      }
      if (pathsJsonObject.keySet().size() > 1) {
        Supplier<String> messageSupplier =
            () ->
                String.format(
                    "the model paths json object [ %s ] may not have more than one root",
                    pathsJsonObject);
        throw new IllegalArgumentException(messageSupplier.get());
      }
      rootEntry =
          pathsJsonObject.entrySet().stream()
              .findFirst()
              .orElseThrow(
                  () ->
                      new IllegalStateException(
                          "look up hashable is not in the correct format: " + pathsJsonObject));
    } catch (Exception e) {
      logger.error(
          String.format(
              "an error occurred when extracting the root model path from json object: [ %s ]",
              pathsJsonObject),
          e);
      throw e;
    }
    String rootFieldName = rootEntry.getKey();
    ModelPath rootPath = ModelPathImpl.builder().addSegments(rootFieldName).build();
    Pair<ModelPath, Map.Entry<String, JsonValue>> rootPathSegment =
        Pair.create(rootPath, rootEntry);
    return UnprocessedModelGraphComponentImpl.of(rootPathSegment);
  }

  private Source<UnprocessedModelGraphComponent, NotUsed>
      createJsonPathsSourceFromUnprocessedRootModelGraphComponent(
          UnprocessedModelGraphComponent rootModelGraphComponent) {
    return Source.from(
        generateIterableOfDescendentUnprocessedModelGraphComponentsFromTopLevelEntry(
            rootModelGraphComponent));
  }

  private Iterable<UnprocessedModelGraphComponent>
      generateIterableOfDescendentUnprocessedModelGraphComponentsFromTopLevelEntry(
          UnprocessedModelGraphComponent topLevelUnprocessedModelGraphComponent) {
    return () -> new UnprocessedModelGraphComponentIterator(topLevelUnprocessedModelGraphComponent);
  }

  private Sink<ModelGraphComponent, CompletionStage<BaseModelGraph>>
      gatherAllProcessedModelGraphComponentsIntoGraph() {
    return Sink.<BaseModelGraphImpl.Builder, ModelGraphComponent>fold(
            BaseModelGraphImpl.builder(),
            (modelGraphBuilder, modelGraphComponent) -> {
              logger.info(
                  "model_graph_components_gathering_sink: component: "
                      + getInterfacesForObjectAsStringListOfNames(modelGraphComponent));
              TypeMatcher.whenTypeOf(modelGraphComponent)
                  .is(ModelVertex.class)
                  .then(vertex -> modelGraphBuilder.putVertex(vertex.vertexPath(), vertex))
                  .is(ModelEdge.class)
                  .then(modelGraphBuilder::addEdge);
              return modelGraphBuilder;
            })
        .mapMaterializedValue(
            builderFuture ->
                builderFuture.handleAsync(
                    (builder1, throwable) -> {
                      if (throwable != null) {
                        throw new RuntimeException(
                            "an error occurred when generating the model graph:",
                            throwable.getCause());
                      } else {
                        return builder1.build();
                      }
                    }));
  }

  private String getInterfacesForObjectAsStringListOfNames(Object obj) {
    return Arrays.stream(obj.getClass().getInterfaces())
        .map(Class::getName)
        .collect(Collectors.joining(", "));
  }

  private Graph<ClosedShape, CompletionStage<BaseModelGraph>>
      buildModelGraphGenerationStreamGraphFromJsonPathsSourceAndModelGraphSink(
          Source<UnprocessedModelGraphComponent, NotUsed> pathsJsonSource,
          Sink<ModelGraphComponent, CompletionStage<BaseModelGraph>>
              modelGraphComponentsGatheringSink) {
    return GraphDSL.create(
        modelGraphComponentsGatheringSink,
        (builder, outputSink) -> {
          Flow<UnprocessedModelGraphComponent, ModelEdge, NotUsed> edgeConversionFlow =
              convertUnprocessedModelGraphComponentsToArgumentOrDirectiveEdgesFlow();
          Flow<
                  UnprocessedModelGraphComponent,
                  Pair<ModelVertexProcessingContext, ModelVertex>,
                  NotUsed>
              vertexConversionFlow =
                  convertUnprocessedModelGraphComponentsToModelProcessingContextAndVertexPairsFlow();

          Flow<Pair<ModelVertexProcessingContext, ModelVertex>, ModelVertex, NotUsed>
              vertexConversionCompletionFlow = Flow.fromFunction(Pair::second);

          Flow<ModelVertex, ModelGraphComponent, NotUsed> upcastVertexToComponentFlow =
              Flow.upcast(Flow.of(ModelVertex.class));

          Flow<ModelEdge, ModelGraphComponent, NotUsed> upcastEdgeToComponentFlow =
              Flow.upcast(Flow.of(ModelEdge.class));

          Outlet<UnprocessedModelGraphComponent> outletSource = builder.add(pathsJsonSource).out();

          UniformFanOutShape<UnprocessedModelGraphComponent, UnprocessedModelGraphComponent>
              unprocessedModelGraphComponentPartition =
                  builder.add(
                      Partition.create(
                          UnprocessedModelGraphComponent.class,
                          2,
                          unprocessedComponent ->
                              isUnprocessedComponentArgumentEdge(unprocessedComponent)
                                      || isUnprocessedComponentDirectiveEdge(unprocessedComponent)
                                  ? 0
                                  : 1));
          builder.from(outletSource).toFanOut(unprocessedModelGraphComponentPartition);
          UniformFanInShape<ModelGraphComponent, ModelGraphComponent> graphComponentMergeFanIn =
              builder.add(
                  Merge.create(ModelGraphComponent.class, 2).named("model_graph_component_merge"));
          builder
              .from(unprocessedModelGraphComponentPartition.out(0))
              .via(builder.add(edgeConversionFlow))
              .via(builder.add(upcastEdgeToComponentFlow))
              .toInlet(graphComponentMergeFanIn.in(0));
          builder
              .from(unprocessedModelGraphComponentPartition.out(1))
              .via(builder.add(vertexConversionFlow))
              .via(builder.add(vertexConversionCompletionFlow))
              .via(builder.add(upcastVertexToComponentFlow))
              .toInlet(graphComponentMergeFanIn.in(1));
          builder.from(graphComponentMergeFanIn.out()).to(outputSink);
          return ClosedShape.getInstance();
        });
  }

  private Flow<UnprocessedModelGraphComponent, ModelEdge, NotUsed>
      convertUnprocessedModelGraphComponentsToArgumentOrDirectiveEdgesFlow() {
    return Flow.of(UnprocessedModelGraphComponent.class)
        .map(this::createArgumentOrDirectiveEdgesFromEntry)
        .mapConcat(list -> list);
  }

  private Flow<
          UnprocessedModelGraphComponent, Pair<ModelVertexProcessingContext, ModelVertex>, NotUsed>
      convertUnprocessedModelGraphComponentsToModelProcessingContextAndVertexPairsFlow() {
    return Flow.of(UnprocessedModelGraphComponent.class)
        .scan(
            Pair.create(ModelVertexProcessingContextImpl.builder().build(), SENTINEL_VERTEX),
            this::convertUnprocessedModelGraphComponentToModelVertexUsingContext)
        .filter(pair -> pair.second() != SENTINEL_VERTEX);
  }

  private void logInitialModelGraph(ModelGraph modelGraph) {
    logger.info(
        "model_graph:\n\tvertices:\n\t\t"
            + modelGraph.asTree().navigableKeySet().stream()
                .map(ModelVertex::vertexPath)
                .map(ModelPath::uri)
                .map(URI::toString)
                .collect(Collectors.joining(",\n\t\t"))
            + "\n\tedges:\n\t\t"
            + modelGraph.edges().stream()
                .sorted(
                    Comparator.comparing(
                        ModelEdge::parentPath, (o1, o2) -> o1.uri().compareTo(o2.uri())))
                .map(
                    edge ->
                        String.join(
                                " --> ",
                                edge.parentPath().uri().toString(),
                                edge.childPath().uri().toString())
                            + ": "
                            + getInterfacesForObjectAsStringListOfNames(edge))
                .collect(Collectors.joining(",\n\t\t")));
  }

  private List<ModelEdge> createArgumentOrDirectiveEdgesFromEntry(
      UnprocessedModelGraphComponent unprocessedModelGraphComponent) {
    if (isUnprocessedComponentArgumentEdge(unprocessedModelGraphComponent)) {
      JsonArray argumentArray = (JsonArray) unprocessedModelGraphComponent.jsonValue();
      List<String> arguments =
          argumentArray.getValuesAs(JsonString.class).stream()
              .map(JsonString::getString)
              .collect(Collectors.toList());
      logger.info("args: " + String.join(", ", arguments));
      try {
        return Arrays.asList(
            UnprocessedArgumentEdgeImpl.builder()
                .parentPath(unprocessedModelGraphComponent.path())
                .childPath(
                    ModelPathImpl.builder()
                        .from(unprocessedModelGraphComponent.path())
                        .rawArguments(arguments)
                        .build())
                .unprocessedArguments(arguments)
                .build());
      } catch (Exception e) {
        logger.error(
            String.format(
                "an error occurred when generating an argument edge for: "
                    + "[ model_path: %s, json_value: %s ]",
                unprocessedModelGraphComponent.path().uri(),
                unprocessedModelGraphComponent.jsonValue()),
            e);
      }
    }

    if (isUnprocessedComponentDirectiveEdge(unprocessedModelGraphComponent)) {
      // TODO: Add Directive entry handling
    }
    return new ArrayList<>();
  }

  private Pair<ModelVertexProcessingContext, ModelVertex>
      convertUnprocessedModelGraphComponentToModelVertexUsingContext(
          Pair<ModelVertexProcessingContext, ModelVertex> contextMostRecentVertexPair,
          UnprocessedModelGraphComponent unprocessedModelGraphComponent) {
    ModelVertexProcessingContext modelVertexProcessingContext = contextMostRecentVertexPair.first();
    Map<ModelPath, ModelVertex> retrievedVertices =
        modelVertexProcessingContext.verticesRetrieved();
    logger.info(
        "vertex_conv: prev_vertices: \n"
            + retrievedVertices.keySet().stream()
                .map(ModelPath::uri)
                .sorted(URI::compareTo)
                .map(URI::toString)
                .collect(Collectors.joining(",\n")));
    logger.info("vertex_conv: unprocessed_vertex: " + unprocessedModelGraphComponent);
    ModelPath vertexPath = unprocessedModelGraphComponent.path();
    if (shouldProcessAsRootVertex(retrievedVertices, vertexPath)) {
      PersistableGraphQLType rootType = getRootType(vertexPath);
      RootVertex rootTypeVertex =
          BaseRootVertexImpl.builder()
              .vertexPath(vertexPath)
              .persistableGraphQlType(rootType)
              .build();
      return Pair.create(
          modelVertexProcessingContext.updateContextWithVertex(rootTypeVertex), rootTypeVertex);
    }
    checkForIllegalArgumentsOrStateForNonRootModelVertexConversion(
        retrievedVertices, unprocessedModelGraphComponent);
    TypeVertex parentTypeVertex = (TypeVertex) retrievedVertices.get(vertexPath.parentPath());
    PersistableGraphQLAttribute persistableGraphQLAttribute =
        findPersistableAttributeThroughParentTypeVertex(vertexPath, parentTypeVertex);
    //        getPersistableGraphQLTypeForAttributeIfApplicable(path, persistableGraphQLAttribute);
    if (!persistableGraphQLAttribute.isBasic()
        && persistableGraphQLAttribute.modelTypeIfAttributeNotBasic().isPresent()) {
      JunctionVertex junctionVertex =
          JunctionVertexImpl.builder()
              .vertexPath(vertexPath)
              .persistableGraphQlType(
                  persistableGraphQLAttribute.modelTypeIfAttributeNotBasic().get())
              .persistableGraphQlAttribute(persistableGraphQLAttribute)
              .build();
      return Pair.create(
          modelVertexProcessingContext.updateContextWithVertex(junctionVertex), junctionVertex);
    } else {
      BasicAttributeVertex basicAttributeVertex =
          BasicAttributeVertexImpl.builder()
              .persistableGraphQlAttribute(persistableGraphQLAttribute)
              .vertexPath(vertexPath)
              .build();
      return Pair.create(
          modelVertexProcessingContext.updateContextWithVertex(basicAttributeVertex),
          basicAttributeVertex);
    }
  }

  private boolean shouldProcessAsRootVertex(
      Map<ModelPath, ModelVertex> retrievedVertices, ModelPath path) {
    return retrievedVertices.size() == 0
        && path.depth() == 1
        && path.rawArguments().size() == 0
        && path.directives().size() == 0;
  }

  private PersistableGraphQLType getRootType(ModelPath rootTypePath) {
    PersistableGraphQLTypeCriteria persistableGraphQLTypeCriteria =
        PersistableGraphQLTypeCriteria.persistableGraphQLType.path.is(rootTypePath);
    PersistableGraphQLType persistableGraphQLType =
        metaModelDatabase
            .getTypeRepository()
            .find(persistableGraphQLTypeCriteria)
            .one()
            .toCompletableFuture()
            .join(); // Add async handling for returning graphql type to avoid blocking
    logger.info(
        "persistableGraphQLType root type: "
            + (persistableGraphQLType == null ? "null" : persistableGraphQLType.slugName()));
    try {
      return Optional.ofNullable(persistableGraphQLType)
          .orElseThrow(
              () ->
                  new NoSuchElementException(
                      String.format(
                          "no root type matching path [ %s ] was found",
                          rootTypePath.uri().toString())));
    } catch (NoSuchElementException e) {
      logger.error(
          String.format("an error occurred when processing vertex [ %s ]", rootTypePath.uri()), e);
      throw e;
    }
  }

  private void checkForIllegalArgumentsOrStateForNonRootModelVertexConversion(
      Map<ModelPath, ModelVertex> retrievedVertices,
      UnprocessedModelGraphComponent unprocessedModelGraphComponent) {
    ModelPath path = unprocessedModelGraphComponent.path();

    if (retrievedVertices.size() == 0 && path.depth() > 1) {
      Supplier<String> messageSupplier =
          () ->
              String.format(
                  "unprocessed model graph component [ %s ] is being processed out of order. "
                      + "the root model graph component must be processed first",
                  path.uri().toString());
      IllegalStateException exception = new IllegalStateException(messageSupplier.get());
      logger.error("an error occurred during model vertex validation: ", exception);
      throw exception;
    }
    if (path.depth() == 0) {
      Supplier<String> messageSupplier =
          () ->
              String.format(
                  "the sentinel model vertex with path [ %s ] should not be processed.",
                  path.uri().toString());
      IllegalStateException exception = new IllegalStateException(messageSupplier.get());
      logger.error("an error occurred during model vertex validation: ", exception);
      throw exception;
    }
    if (path.rawArguments().size() > 0 || path.directives().size() > 0) {
      Supplier<String> messageSupplier =
          () ->
              String.format(
                  "unprocessed model graph component [ %s ] represents an argument and/or directive and "
                      + "not purely a type or attribute on a type, so it may not be converted into a model vertex",
                  path.uri().toString());
      IllegalArgumentException exception = new IllegalArgumentException(messageSupplier.get());
      logger.error("an error occurred during model vertex validation: ", exception);
      throw exception;
    }
    if (path.depth() == 0) {
      Supplier<String> messageSupplier =
          () ->
              String.format(
                  "unprocessed model graph component [ %s ] cannot be looked up and converted "
                      + "to a model vertex as no type name can be obtained from this path",
                  path);
      IllegalArgumentException exception = new IllegalArgumentException(messageSupplier.get());
      logger.error("an error occurred during model vertex validation: ", exception);
      throw exception;
    }
    if (retrievedVertices.containsKey(path)) {
      Supplier<String> messageSupplier =
          () ->
              String.format(
                  "unprocessed model graph component [ %s ] has already been mapped to vertex [ %s ]",
                  unprocessedModelGraphComponent, retrievedVertices.get(path).vertexPath().uri());
      IllegalArgumentException exception = new IllegalArgumentException(messageSupplier.get());
      logger.error("an error occurred during model vertex validation: ", exception);
      throw exception;
    }
    if (!retrievedVertices.containsKey(path.parentPath())) {
      Supplier<String> messageSupplier =
          () ->
              String.format(
                  "unprocessed model component [ %s ] has not had its parent path "
                      + "mapped to a vertex. the entries are being processed out of order.",
                  path.uri().toString());
      IllegalStateException exception = new IllegalStateException(messageSupplier.get());
      logger.error("an error occurred during model vertex validation: ", exception);
      throw exception;
    }
    if (!(retrievedVertices.get(path.parentPath()) instanceof TypeVertex)) {
      Supplier<String> messageSupplier =
          () ->
              String.format(
                  "unprocessed model component [ %s ] has a parent vertex that has not been mapped to a type [ %s ] ",
                  path.uri().toString(), retrievedVertices.get(path.parentPath()));
      IllegalStateException exception = new IllegalStateException(messageSupplier.get());
      logger.error("an error occurred during model vertex validation: ", exception);
      throw exception;
    }
  }

  private PersistableGraphQLAttribute findPersistableAttributeThroughParentTypeVertex(
      ModelPath childAttributeVertexPath, TypeVertex parentTypeVertex) {
    ModelPath modelAttributePath =
        ModelPathImpl.builder()
            .from(parentTypeVertex.persistableGraphQlType().path())
            .addSegment(childAttributeVertexPath.lastSegment())
            .build();
    if (!parentTypeVertex.persistableGraphQlType().attributePaths().contains(modelAttributePath)) {
      Supplier<String> messageSupplier =
          () ->
              String.format(
                  "attribute path [ %s ] derived from parent type vertex [ %s ] and last segment of "
                      + "child attribute vertex path [ %s ] was not found on model type [ %s ]",
                  modelAttributePath.uri().toString(),
                  parentTypeVertex.vertexPath().uri().toString(),
                  childAttributeVertexPath.uri().toString(),
                  parentTypeVertex.persistableGraphQlType().path().uri().toString());
      NoSuchElementException exception = new NoSuchElementException(messageSupplier.get());
      logger.error(messageSupplier.get(), exception);
      throw exception;
    }
    Optional<PersistableGraphQLAttribute> attributeMaybeFoundThroughParent =
        metaModelDatabase
            .getAttributeRepository()
            .find(
                PersistableGraphQLAttributeCriteria.persistableGraphQLAttribute.path.is(
                    modelAttributePath))
            .oneOrNone()
            .toCompletableFuture()
            .join();
    if (!attributeMaybeFoundThroughParent.isPresent()) {
      Supplier<String> messageSupplier =
          () ->
              String.format(
                  "the path [ %s ] was not found as a child attribute on parent vertex [ %s ] with name [ %s ]",
                  childAttributeVertexPath.uri().toString(),
                  parentTypeVertex.persistableGraphQlType().path().uri(),
                  childAttributeVertexPath.lastSegment());
      NoSuchElementException exception = new NoSuchElementException(messageSupplier.get());
      logger.error("an error occurred during vertex processing: ", exception);
      throw exception;
    }
    return attributeMaybeFoundThroughParent.get();
  }

  private static class UnprocessedModelGraphComponentIterator
      implements Iterator<UnprocessedModelGraphComponent> {

    private final UnprocessedModelGraphComponent topLevelUnprocessedComponent;
    private final LinkedList<UnprocessedModelGraphComponent> stack;

    public UnprocessedModelGraphComponentIterator(
        UnprocessedModelGraphComponent topLevelUnprocessedComponent) {
      this.topLevelUnprocessedComponent = topLevelUnprocessedComponent;
      this.stack = new LinkedList<>();
      this.stack.push(topLevelUnprocessedComponent);
    }

    @Override
    public boolean hasNext() {
      return !stack.isEmpty();
    }

    @Override
    public UnprocessedModelGraphComponent next() {
      if (!stack.isEmpty()) {
        return extrapolateDescendents();
      }
      NoSuchElementException exception =
          new NoSuchElementException(
              "No more unprocessed model graph components can be generated from top level component: "
                  + topLevelUnprocessedComponent);
      //      logger.error("iterator_done: ", exception);
      throw exception;
    }

    private UnprocessedModelGraphComponent extrapolateDescendents() {
      UnprocessedModelGraphComponent unprocessedComponent = stack.pop();
      //      logger.info(
      //          "unprocessed_comp: "
      //              + unprocessedComponent.path().uri()
      //              + " type: "
      //              +
      // (unprocessedComponent.jsonValue().getValueType().equals(JsonValue.ValueType.ARRAY)
      //                  ? "arg or dir"
      //                  : "field"));
      if (isUnprocessedComponentAnEdge(unprocessedComponent)) {
        return unprocessedComponent;
      }
      ModelPath parentPath = unprocessedComponent.path();
      JsonObject jsonNode = (JsonObject) unprocessedComponent.jsonValue();
      jsonNode.entrySet().stream()
          .map(
              stringJsonValueEntry ->
                  determineChildUnprocessedComponentTypeForCurrentUnprocessedComponent(
                      Pair.create(parentPath, stringJsonValueEntry)))
          .forEach(stack::add);
      return unprocessedComponent;
    }

    private boolean isUnprocessedComponentAnEdge(
        UnprocessedModelGraphComponent unprocessedComponent) {
      return isUnprocessedComponentArgumentEdge(unprocessedComponent)
          || isUnprocessedComponentDirectiveEdge(unprocessedComponent);
    }

    private UnprocessedModelGraphComponent
        determineChildUnprocessedComponentTypeForCurrentUnprocessedComponent(
            Pair<ModelPath, Map.Entry<String, JsonValue>> nextJsonPathsEntry) {
      UnprocessedModelGraphComponent childUnprocessedComponent =
          UnprocessedModelGraphComponentImpl.of(nextJsonPathsEntry);
      if (isUnprocessedComponentAnEdge(childUnprocessedComponent)) {
        return childUnprocessedComponent;
      } else {
        return UnprocessedModelGraphComponentImpl.of(
            Pair.create(
                ModelPathImpl.builder()
                    .from(childUnprocessedComponent.path())
                    .addSegment(childUnprocessedComponent.segmentName())
                    .build(),
                childUnprocessedComponent.segmentEntry().second()));
      }
    }

    //    private Logger logger =
    // LoggerFactory.getLogger(UnprocessedModelGraphComponentGenerator.class);

  }
}

//    Sink<ModelGraphComponent, NotUsed> modelGraphComponentSink =
//        Flow.of(ModelGraphComponent.class)
//            .to(
//                Sink.<ModelGraphComponent>foreach(
//                    component -> {
//                      if (component instanceof ModelVertex) {
//                        logger.info(
//                            "vertex_component sink: " + ((ModelVertex) component).vertexPath());
//                      } else if (component instanceof ModelEdge) {
//                        logger.info(
//                            "edge_component sink: "
//                                + ((ModelEdge) component).parentPath().uri().toString()
//                                + " --> "
//                                + ((ModelEdge) component).childPath().uri().toString());
//                      } else {
//                        logger.info("other_component: " + component.toString());
//                      }
//                    }))
//            .named("model_graph_component_print_out_sink");
//
//
//  Flow<
//          UnprocessedModelGraphComponent,
//          Pair<ModelVertexProcessingContext, ModelVertex>,
//          NotUsed>
//          vertexConversionFlow =
//          Flow.of(UnprocessedModelGraphComponent.class)
//                  .scan(
//                          Pair.create(
//                                  ModelVertexProcessingContextImpl.builder().build(),
//                                  SENTINEL_VERTEX),
//                          this::convertUnprocessedModelGraphComponentToModelVertexUsingContext)
//                  .filter(pair -> pair.second() != SENTINEL_VERTEX)
//                  .map(
//                          pair -> {
//                            logger.info(
//                                    String.format(
//                                            "[ retrv_vert_map_size: %d, mod_vert_path: %s ]",
//                                            pair.first().verticesRetrieved().size(),
//                                            pair.second().vertexPath().uri().toString()));
//                            return pair;
//                          });

//  private Optional<PersistableGraphQLType> getPersistableGraphQLTypeForAttributeIfApplicable(
//          ModelPath path, PersistableGraphQLAttribute persistableGraphQLAttribute) {
//    Optional<PersistableGraphQLType> persistableGraphQLTypeIfApplicable = Optional.empty();
//    if (!persistableGraphQLAttribute.isBasic()) {
//      PersistableGraphQLTypeCriteria persistableGraphQLTypeCriterion =
//              PersistableGraphQLTypeCriteria.persistableGraphQLType.javaType.is(
//                      persistableGraphQLAttribute.singularTypeOrPluralAttributeElementType());
//      Optional<PersistableGraphQLType> persistableGraphQLTypeFound =
//              metaModelDatabase
//                      .getTypeRepository()
//                      .find(persistableGraphQLTypeCriterion)
//                      .oneOrNone()
//                      .toCompletableFuture()
//                      .join();
//      if (!persistableGraphQLTypeFound.isPresent()) {
//        Supplier<String> messageSupplier =
//                () ->
//                        String.format(
//                                "type with path [ %s ] and managed type name [ %s ] was not
// found",
//                                path.uri().toString(),
//
// persistableGraphQLAttribute.parentJpaManagedType().getJavaType().getName());
//        NoSuchElementException exception = new NoSuchElementException(messageSupplier.get());
//        logger.error(messageSupplier.get(), exception);
//      }
//      persistableGraphQLTypeIfApplicable = persistableGraphQLTypeFound;
//    }
//    return persistableGraphQLTypeIfApplicable;
//  }
