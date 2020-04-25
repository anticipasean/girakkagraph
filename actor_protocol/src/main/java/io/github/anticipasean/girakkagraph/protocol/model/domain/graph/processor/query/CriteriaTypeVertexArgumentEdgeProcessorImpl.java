package io.github.anticipasean.girakkagraph.protocol.model.domain.graph.processor.query;

import io.github.anticipasean.girakkagraph.protocol.model.domain.argument.jpa.CriteriaJpaPredicateArgument;
import io.github.anticipasean.girakkagraph.protocol.model.domain.argument.jpa.CriteriaJpaPredicateArgumentImpl;
import io.github.anticipasean.girakkagraph.protocol.model.domain.context.graph.QueryGraphOperatorProcessingContext;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.ModelVertex;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.QueryModelGraph;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.UnprocessedArgumentEdge;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.FromObjectEdge;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.JpaCriteriaArgumentEdge;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.RootFromVertex;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.SelectionPredicateArgumentEdgeImpl;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.vertex.JunctionVertex;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.vertex.RootVertex;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.vertex.TypeVertex;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.ModelPath;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.ModelPathImpl;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operandset.jpa.JpaCriteriaOperandSet;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operator.Operator;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operator.jpa.JpaComparativeOperator;
import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.PredicateSupplier;
import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.SingularJpaAttributeSupplier;
import io.github.anticipasean.girakkagraph.typematcher.TypeMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CriteriaTypeVertexArgumentEdgeProcessorImpl
    implements CriteriaTypeVertexArgumentEdgeProcessor {
  private Logger logger;

  public CriteriaTypeVertexArgumentEdgeProcessorImpl() {
    this.logger = LoggerFactory.getLogger(CriteriaTypeVertexArgumentEdgeProcessorImpl.class);
  }

  @Override
  public QueryGraphOperatorProcessingContext updateContextAccordingToComponent(
      QueryGraphOperatorProcessingContext context, UnprocessedArgumentEdge component) {
    ModelPath componentParentPath = component.parentPath();
    checkComponentParentHasBeenProcessed(context, componentParentPath);
    ModelVertex parentVertex = context.baseModelGraph().vertices().get(componentParentPath);
    return TypeMatcher.whenTypeOf(parentVertex)
        .is(TypeVertex.class)
        .thenApply(
            typeVertex ->
                inferArgumentTypesForTypeVertexFromContextAndUnprocessedArgumentEdge(
                    typeVertex, context, component))
        .orElseThrow(notTypeVertexArgumentEdgeExceptionSupplier(parentVertex));
  }

  private void checkComponentParentHasBeenProcessed(
      QueryGraphOperatorProcessingContext context, ModelPath componentParentPath) {
    if (!context.baseModelGraph().vertices().containsKey(componentParentPath)) {
      Supplier<String> messageSupplier =
          () ->
              String.format(
                  "parent component [ %s ] has not been processed and does not"
                      + " contain an entry in the base model graph. cannot complete "
                      + "the processing of this argument edge",
                  componentParentPath.uri());
      throw new IllegalStateException(messageSupplier.get());
    }
  }

  private <X, Y>
      QueryGraphOperatorProcessingContext
          inferArgumentTypesForTypeVertexFromContextAndUnprocessedArgumentEdge(
              TypeVertex typeVertex,
              QueryGraphOperatorProcessingContext context,
              UnprocessedArgumentEdge unprocessedArgumentEdge) {
    logger.info(
        String.format(
            "type_vertex: %s, unprocessed_arg_edge: %s",
            typeVertex.vertexPath().uri(), unprocessedArgumentEdge.edgeKey()));
    if (typeVertex.persistableGraphQlType().isEntity()) {
      Optional<? extends SingularAttribute<? super X, Y>> singularIdAttributeIfApt =
          typeVertex
              .persistableGraphQlType()
              .singularIdAttributeSupplierIfApt()
              .map(SingularJpaAttributeSupplier::singularAttribute);
      if (singularIdAttributeIfApt.isPresent()) {
        String normalizedAttributeName =
            ModelPath.normalizeSegment(singularIdAttributeIfApt.get().getName());
        ModelPath attributePath =
            ModelPathImpl.builder()
                .segments(typeVertex.vertexPath().segments())
                .addSegment(normalizedAttributeName)
                .build();
        SingularAttribute<? super X, Y> singularAttribute = singularIdAttributeIfApt.get();
        if (context.queryCriteriaEdgesProcessingMap().get(QueryModelGraph.SQL.ROOT).size() != 0
            && context
                .queryCriteriaEdgesProcessingMap()
                .get(QueryModelGraph.SQL.ROOT)
                .firstKey()
                .parentPath()
                .equals(attributePath.parentPath())) {
          Root<X> rootFromObject =
              ((RootFromVertex)
                      context
                          .queryCriteriaEdgesProcessingMap()
                          .get(QueryModelGraph.SQL.ROOT)
                          .firstEntry()
                          .getValue())
                  .rootFromExpressionSupplier()
                  .rootFromObject();
          Path<Y> attributePathExpression = rootFromObject.get(singularAttribute);

          Operator<JpaCriteriaOperandSet, PredicateSupplier> operator =
              JpaComparativeOperator.Binary.EQUAL_TO_EXPRESSION;
          JpaCriteriaOperandSet jpaCriteriaBinaryOperandSet =
              JpaCriteriaOperandSet.ofField(attributePathExpression)
                  .andValueType(attributePathExpression.getJavaType())
                  .buildBinarySet(context.criteriaBuilder());
          //          if (!operator.operandSetValidityTest().test(jpaCriteriaBinaryOperandSet)) {
          //            // TODO: handle failed validity test
          //          }

          CriteriaJpaPredicateArgument binaryCriteriaJpaArgument =
              CriteriaJpaPredicateArgumentImpl.builder()
                  .operandSet(jpaCriteriaBinaryOperandSet)
                  .operator(operator)
                  .build();
          SelectionPredicateArgumentEdgeImpl.builder()
              .criteriaJpaArgument(binaryCriteriaJpaArgument)
              .parentPath(typeVertex.vertexPath())
              .childPath(
                  typeVertex.vertexPath().withParameterizedArgument(attributePath.lastSegment()))
              .build();
        }
        Optional<FromObjectEdge> fromObjectEdgeMaybe =
            context
                .queryCriteriaEdgesProcessingMap()
                .findFromObjectOnWhichPathCanBeSelectedIfPresent(attributePath);
        if (fromObjectEdgeMaybe.isPresent()) {}
      }
    }
    return context.sameContextButNoComponents();
  }

  private Supplier<IllegalArgumentException> notTypeVertexArgumentEdgeExceptionSupplier(
      ModelVertex parentVertex) {
    return () ->
        new IllegalArgumentException(
            String.format(
                "component parent vertex [ %s ] is not a Type Vertex and thus "
                    + "should not be passed to this processor",
                parentVertex.vertexPath().uri().toString()));
  }

  private List<JpaCriteriaArgumentEdge>
      inferArgumentTypesForRootVertexFromContextAndUnprocessedArgumentEdge(
          RootVertex rootVertex,
          QueryGraphOperatorProcessingContext context,
          UnprocessedArgumentEdge unprocessedArgumentEdge) {
    logger.info(
        String.format(
            "root_vertex: %s, unprocessed_arg_edge: %s",
            rootVertex.vertexPath().uri().toString(), unprocessedArgumentEdge.edgeKey()));
    return new ArrayList<>();
  }

  private java.util.List<JpaCriteriaArgumentEdge>
      inferArgumentTypesForJunctionVertexFromContextAndUnprocessedArgumentEdge(
          JunctionVertex junctionVertex,
          QueryGraphOperatorProcessingContext context,
          UnprocessedArgumentEdge unprocessedArgumentEdge) {
    logger.info(
        String.format(
            "junction_vertex: %s, unprocessed_arg_edge: %s",
            junctionVertex.vertexPath().uri(), unprocessedArgumentEdge.edgeKey()));
    return new ArrayList<>();
  }

}
