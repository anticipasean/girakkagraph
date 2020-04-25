package io.github.anticipasean.girakkagraph.protocol.model.domain.graph.processor.query;

import io.github.anticipasean.girakkagraph.protocol.model.domain.argument.jpa.CriteriaJpaArgument;
import io.github.anticipasean.girakkagraph.protocol.model.domain.context.graph.QueryGraphOperatorProcessingContext;
import io.github.anticipasean.girakkagraph.protocol.model.domain.context.graph.QueryGraphOperatorProcessingContextImpl;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.ModelGraphComponent;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.ModelVertex;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.UnprocessedArgumentEdge;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.FromObjectEdge;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.JpaCriteriaArgumentEdge;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.vertex.AttributeVertex;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.vertex.BasicAttributeVertex;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.vertex.ParameterVertex;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.ModelPath;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operandset.jpa.JpaCriteriaOperandSet;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operator.jpa.JpaCriteriaOperator;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operator.jpa.JpaCriteriaOperators;
import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.PluralJpaAttributeSupplier;
import io.github.anticipasean.girakkagraph.typematcher.TypeMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.metamodel.MapAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CriteriaBasicAttributeVertexArgumentEdgeProcessorImpl
    implements CriteriaBasicAttributeVertexArgumentEdgeProcessor {

  private final Map<String, JpaCriteriaOperator<?>> callNameToOperatorSet;
  private final Logger logger;
  private final OperatorOperandSetToJpaCriteriaArgumentMapper jpaOperatorOperandSetToArgumentMapper;
  private final ArgumentToArgumentEdgeMapper jpaArgumentToArgumentEdgeMapper;
  private final QueryParameterVertexCreator queryParameterVertexCreator;

  public CriteriaBasicAttributeVertexArgumentEdgeProcessorImpl() {
    this.callNameToOperatorSet = JpaCriteriaOperators.newCallNameToOperatorMapInstance();
    this.logger =
        LoggerFactory.getLogger(CriteriaBasicAttributeVertexArgumentEdgeProcessorImpl.class);
    this.jpaOperatorOperandSetToArgumentMapper =
        OperatorOperandSetToJpaCriteriaArgumentMapper.newInstance();
    this.jpaArgumentToArgumentEdgeMapper = ArgumentToArgumentEdgeMapper.newInstance();
    this.queryParameterVertexCreator = QueryParameterVertexCreator.newInstance();
  }

  @Override
  public QueryGraphOperatorProcessingContext updateContextAccordingToComponent(
      QueryGraphOperatorProcessingContext context, UnprocessedArgumentEdge component) {
    ModelPath attributeOrTypeVertexPath = component.parentPath();
    checkParentComponentHasBeenProcessed(context, attributeOrTypeVertexPath);
    ModelVertex attributeOrTypeVertex =
        context.baseModelGraph().vertices().get(attributeOrTypeVertexPath);
    return TypeMatcher.whenTypeOf(attributeOrTypeVertex)
        .is(BasicAttributeVertex.class)
        .thenApply(
            basicAttributeVertex ->
                createJpaCriteriaArgumentEdgeFromUnprocessedArgumentEdgeAndContext(
                    basicAttributeVertex, component, context))
        .orElseThrow(
            componentNotMatchingExpectedComponentTypesExceptionSupplier(
                component, attributeOrTypeVertexPath));
  }

  private Supplier<IllegalStateException>
      componentNotMatchingExpectedComponentTypesExceptionSupplier(
          UnprocessedArgumentEdge component, ModelPath attributeOrTypeVertexPath) {
    return () ->
        new IllegalStateException(
            String.format(
                "unprocessed argument edge [ %s ] for model vertex [ %s ] could not be processed since "
                    + "its type did not match one of those expected.",
                component.edgeKey(), attributeOrTypeVertexPath.uri()));
  }

  private void checkParentComponentHasBeenProcessed(
      QueryGraphOperatorProcessingContext context, ModelPath attributeOrTypeVertexPath) {
    if (!context.baseModelGraph().vertices().containsKey(attributeOrTypeVertexPath)) {
      // TODO: handle vertex not found
      throw new IllegalStateException(
          String.format(
              "base model graph does not contain an entry for vertex path [ %s ]",
              attributeOrTypeVertexPath.uri()));
    }
  }

  private QueryGraphOperatorProcessingContext
      createJpaCriteriaArgumentEdgeFromUnprocessedArgumentEdgeAndContext(
          BasicAttributeVertex basicAttributeVertex,
          UnprocessedArgumentEdge unprocessedArgumentEdge,
          QueryGraphOperatorProcessingContext context) {
    List<JpaCriteriaArgumentEdge> jpaCriteriaArgumentEdges =
        inferArgumentEdgesForBasicAttributeVertexFromContextAndUnprocessedArgumentEdge(
            basicAttributeVertex, context, unprocessedArgumentEdge);
    List<ParameterVertex> parameterVertices =
        jpaCriteriaArgumentEdges.stream()
            .map(queryParameterVertexCreator)
            .collect(Collectors.toList());
    logger.info(
        "argument edges created: [ "
            + jpaCriteriaArgumentEdges.stream()
                .map(
                    jpaCriteriaArgumentEdge ->
                        String.join(
                            " : ",
                            jpaCriteriaArgumentEdge.edgeKey().toString(),
                            jpaCriteriaArgumentEdge.getClass().getTypeName()))
                .collect(Collectors.joining(", "))
            + " ]");
    logger.info(
        "query_parameter_vertices_created: [ \n\t"
            + parameterVertices.stream()
                .map(parameterVertex -> parameterVertex.vertexPath().uri().toString())
                .collect(Collectors.joining(",\n\t"))
            + "\n]");
    return ((QueryGraphOperatorProcessingContextImpl) context)
        .withCurrentRoundComponents(
            () ->
                Stream.<ModelGraphComponent>concat(
                        jpaCriteriaArgumentEdges.stream(), parameterVertices.stream())
                    .iterator());
  }

  private List<JpaCriteriaArgumentEdge>
      inferArgumentEdgesForBasicAttributeVertexFromContextAndUnprocessedArgumentEdge(
          BasicAttributeVertex basicAttributeVertex,
          QueryGraphOperatorProcessingContext context,
          UnprocessedArgumentEdge unprocessedArgumentEdge) {
    logger.info(
        String.format(
            "basic_attr_vertex: %s, unprocessed_arg_edge: %s",
            basicAttributeVertex.vertexPath().uri(), unprocessedArgumentEdge.edgeKey()));
    FromObjectEdge selectableFromObjectEdge =
        getSelectableFromObjectEdgefromContext(basicAttributeVertex, context);
    Expression<?> attributeFieldExpression =
        selectAttributeExpression(basicAttributeVertex, selectableFromObjectEdge);
    List<CriteriaJpaArgument<?>> argumentsCreated = new ArrayList<>();
    for (String rawOperatorCallName : unprocessedArgumentEdge.unprocessedArguments()) {
      if (callNameToOperatorSet.containsKey(rawOperatorCallName)) {
        JpaCriteriaOperator<?> operator = callNameToOperatorSet.get(rawOperatorCallName);
        logger.info(
            String.format(
                "operator matching raw call name [ %s ]: %s",
                rawOperatorCallName, operator.name()));
        JpaCriteriaOperandSet jpaCriteriaOperandSet =
            createOperandSetForJpaCriteriaOperatorFromContextAttributeExpressionAndVertex(
                operator, context, attributeFieldExpression, basicAttributeVertex);
        checkOperandSetMeetsOperatorRestrictions(
            basicAttributeVertex, operator, jpaCriteriaOperandSet);
        CriteriaJpaArgument<?> criteriaJpaArgument =
            jpaOperatorOperandSetToArgumentMapper.apply(operator, jpaCriteriaOperandSet);
        argumentsCreated.add(criteriaJpaArgument);
      } else {
        return handleUnknownOperator(basicAttributeVertex, rawOperatorCallName);
      }
    }
    return mapCriteriaJpaArgumentsToJpaCriteriaArgumentEdges(
        argumentsCreated, basicAttributeVertex, context);
  }

  private FromObjectEdge getSelectableFromObjectEdgefromContext(
      ModelVertex modelVertex, QueryGraphOperatorProcessingContext context) {
    Optional<FromObjectEdge> selectableFromObjectEdgeIfApt =
        context
            .queryCriteriaEdgesProcessingMap()
            .findFromObjectOnWhichPathCanBeSelectedIfPresent(modelVertex.vertexPath());
    if (!selectableFromObjectEdgeIfApt.isPresent()) {
      throw new IllegalStateException(
          String.format(
              "no selectable from object found for model vertex: [ %s ];"
                  + " this vertex may not have been processed in order",
              modelVertex));
    }
    return selectableFromObjectEdgeIfApt.get();
  }

  @SuppressWarnings("unchecked")
  private <X> Expression<X> selectAttributeExpression(
      BasicAttributeVertex basicAttributeVertex, FromObjectEdge selectableFromObjectEdge) {
    checkBasicAttributeOnVertexParentJpaManagedTypeAssignableToSelectableFromObjectEdgeType(
        basicAttributeVertex, selectableFromObjectEdge);
    if (basicAttributeVertex.persistableGraphQlAttribute().isSingular()) {
      return selectableFromObjectEdge
          .fromObjectSupplier()
          .fromObject()
          .get(
              basicAttributeVertex
                  .persistableGraphQlAttribute()
                  .singularJpaAttributeSupplierIfApt()
                  .get()
                  .singularAttribute());
    } else {
      if (basicAttributeVertex.persistableGraphQlAttribute().isMap()) {
        return (Expression<X>)
            selectMapExpressionUsingAttributeVertexOnSelectableFromObjectEdge(
                basicAttributeVertex, selectableFromObjectEdge);
      } else {
        return (Expression<X>)
            selectPluralExpressionUsingAttributeVertexOnSelectableFromObjectEdge(
                basicAttributeVertex, selectableFromObjectEdge);
      }
    }
  }

  private void
      checkBasicAttributeOnVertexParentJpaManagedTypeAssignableToSelectableFromObjectEdgeType(
          BasicAttributeVertex basicAttributeVertex, FromObjectEdge selectableFromObjectEdge) {
    if (!basicAttributeVertex
        .persistableGraphQlAttribute()
        .jpaAttribute()
        .getDeclaringType()
        .getJavaType()
        .isAssignableFrom(
            selectableFromObjectEdge.fromObjectSupplier().fromObject().getJavaType())) {
      Supplier<String> messageSupplier =
          () ->
              String.format(
                  "the parent jpa managed type [ %s ] of basic attribute vertex [ vertex_path: %s , "
                      + "persistable_attrib_path: %s , jpa_attribute_name: %s  ] is not assignable "
                      + "to jpa from object type [ %s ]",
                  basicAttributeVertex
                      .persistableGraphQlAttribute()
                      .jpaAttribute()
                      .getDeclaringType()
                      .getJavaType()
                      .getSimpleName(),
                  basicAttributeVertex.vertexPath().uri(),
                  basicAttributeVertex.persistableGraphQlAttribute().path().uri(),
                  basicAttributeVertex.persistableGraphQlAttribute().jpaAttribute().getName(),
                  selectableFromObjectEdge
                      .fromObjectSupplier()
                      .fromObject()
                      .getJavaType()
                      .getSimpleName());
      throw new IllegalStateException(messageSupplier.get());
    }
  }

  private <X, K, V, M extends java.util.Map<K, V>>
      Expression<M> selectMapExpressionUsingAttributeVertexOnSelectableFromObjectEdge(
          AttributeVertex attributeVertex, FromObjectEdge selectableFromObjectEdge) {
    From<?, X> fromObject = selectableFromObjectEdge.fromObjectSupplier().fromObject();
    PluralJpaAttributeSupplier pluralJpaAttributeSupplier =
        attributeVertex.persistableGraphQlAttribute().pluralJpaAttributeSupplierIfApt().get();
    MapAttribute<X, K, V> mapAttribute =
        pluralJpaAttributeSupplier.<X, M, K, V>asMapAttribute().get();
    return fromObject.get(mapAttribute);
  }

  private <E, C extends java.util.Collection<E>>
      Expression<C> selectPluralExpressionUsingAttributeVertexOnSelectableFromObjectEdge(
          AttributeVertex attributeVertex, FromObjectEdge selectableFromObjectEdge) {
    return selectableFromObjectEdge
        .fromObjectSupplier()
        .fromObject()
        .get(
            attributeVertex
                .persistableGraphQlAttribute()
                .pluralJpaAttributeSupplierIfApt()
                .get()
                .pluralAttribute());
  }

  private JpaCriteriaOperandSet
      createOperandSetForJpaCriteriaOperatorFromContextAttributeExpressionAndVertex(
          JpaCriteriaOperator<?> operator,
          QueryGraphOperatorProcessingContext context,
          Expression<?> attributeFieldExpression,
          BasicAttributeVertex basicAttributeVertex) {
    switch (operator.arity()) {
      case NULLARY:
        throw new UnsupportedOperationException(
            String.format(
                "no nullary operators in this set are set up for this context: %s",
                operator.name()));
      case UNARY:
        return JpaCriteriaOperandSet.ofField(attributeFieldExpression)
            .buildUnarySet(context.criteriaBuilder());
      case BINARY:
        return JpaCriteriaOperandSet.ofField(attributeFieldExpression)
            .andValueType(
                basicAttributeVertex.persistableGraphQlAttribute().jpaAttribute().getJavaType())
            .buildBinarySet(context.criteriaBuilder());
      case TERNARY:
        return JpaCriteriaOperandSet.ofField(attributeFieldExpression)
            .andValueType(
                basicAttributeVertex.persistableGraphQlAttribute().jpaAttribute().getJavaType())
            .lastValueType(
                basicAttributeVertex.persistableGraphQlAttribute().jpaAttribute().getJavaType())
            .buildTernarySet(context.criteriaBuilder());
      default:
        throw new NoSuchElementException(
            "no operators have more than ternary arity in the set coded");
    }
  }

  private void checkOperandSetMeetsOperatorRestrictions(
      BasicAttributeVertex basicAttributeVertex,
      JpaCriteriaOperator<?> operator,
      JpaCriteriaOperandSet jpaCriteriaOperandSet) {
    if (!operator.meetsRestrictions(jpaCriteriaOperandSet)) {
      // TODO: handle operandset validity test failed
      throw new IllegalStateException(
          String.format(
              "operandset validity test failed for operator [ %s ] for basic attribute vertex [ %s ]",
              operator.name(), basicAttributeVertex.vertexPath().uri()));
    }
  }

  private List<JpaCriteriaArgumentEdge> handleUnknownOperator(
      BasicAttributeVertex basicAttributeVertex, String rawOperatorCallName) {
    BiFunction<BasicAttributeVertex, String, String> operatorUnknownMessageCreator =
        (basicAttributeVertex1, rawOperatorCallName1) ->
            String.format(
                "unknown argument operator call name received [ %s ] for basic attribute vertex [ %s ]",
                rawOperatorCallName1, basicAttributeVertex1.vertexPath().uri());
    String message = operatorUnknownMessageCreator.apply(basicAttributeVertex, rawOperatorCallName);
    IllegalStateException illegalStateException = new IllegalStateException(message);
    logger.error(message, illegalStateException);
    throw illegalStateException;
  }

  private List<JpaCriteriaArgumentEdge> mapCriteriaJpaArgumentsToJpaCriteriaArgumentEdges(
      List<CriteriaJpaArgument<?>> arguments,
      BasicAttributeVertex basicAttributeVertex,
      QueryGraphOperatorProcessingContext context) {
    return arguments.stream()
        .map(
            criteriaJpaArgument ->
                jpaArgumentToArgumentEdgeMapper.apply(basicAttributeVertex, criteriaJpaArgument))
        .map(
            jpaCriteriaArgumentEdge -> {
              context.updateWithJpaCriteriaEdge(jpaCriteriaArgumentEdge);
              return jpaCriteriaArgumentEdge;
            })
        .collect(Collectors.toList());
  }
}
