package io.github.anticipasean.girakkagraph.protocol.model.domain.graph.processor.query;

import io.github.anticipasean.girakkagraph.protocol.model.domain.context.graph.QueryGraphOperatorProcessingContext;
import io.github.anticipasean.girakkagraph.protocol.model.domain.context.graph.QueryGraphOperatorProcessingContextImpl;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.QueryModelGraph;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.FromObjectEdge;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.SelectionEdge;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.SelectionEdgeImpl;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.vertex.BasicAttributeVertex;
import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.JpaSelectionSupplier;
import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.PluralJpaAttributeSupplier;
import io.github.anticipasean.girakkagraph.typematcher.TypeMatcher;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CriteriaSelectionEdgeProcessorImpl implements CriteriaSelectionEdgeProcessor {
  private final Logger logger;

  public CriteriaSelectionEdgeProcessorImpl() {
    this.logger = LoggerFactory.getLogger(CriteriaSelectionEdgeProcessorImpl.class);
  }

  @Override
  public QueryGraphOperatorProcessingContext updateContextAccordingToComponent(
      QueryGraphOperatorProcessingContext context, BasicAttributeVertex component) {
    return TypeMatcher.whenTypeOf(component)
        .is(BasicAttributeVertex.class)
        .thenApply(
            basicAttributeVertex ->
                createSelectionEdgeFromContextAndBasicAttributeVertex(
                    context, basicAttributeVertex))
        .orElse(context.sameContextButNoComponents());
  }

  private QueryGraphOperatorProcessingContext createSelectionEdgeFromContextAndBasicAttributeVertex(
      QueryGraphOperatorProcessingContext queryGraphProcessingContext,
      BasicAttributeVertex basicAttributeVertex) {
    logger.info("creating selection edge from: " + basicAttributeVertex.vertexPath().uri());
    Optional<FromObjectEdge> fromObjectEdgeMaybe =
        queryGraphProcessingContext
            .queryCriteriaEdgesProcessingMap()
            .findFromObjectOnWhichPathCanBeSelectedIfPresent(basicAttributeVertex.vertexPath());
    if (!fromObjectEdgeMaybe.isPresent()) {
      logger.error(
          "no from object edge was found on which this basic attribute vertex can be selected: "
              + basicAttributeVertex.vertexPath());
    }
    FromObjectEdge fromObjectEdge = fromObjectEdgeMaybe.get();
    SelectionEdge selectionEdge = null;
    if (basicAttributeVertex.persistableGraphQlAttribute().isSingular()) {
      selectionEdge =
          processSingularBasicAttributeVertexAsSelectionEdgeOnFromObjectEdge(
              queryGraphProcessingContext, basicAttributeVertex, fromObjectEdge);
    } else {
      selectionEdge =
          processPluralBasicAttributeVertexAsSelectionEdgeOnFromObjectEdge(
              queryGraphProcessingContext, basicAttributeVertex, fromObjectEdge);
    }
    queryGraphProcessingContext
        .queryCriteriaEdgesProcessingMap()
        .putEdge(QueryModelGraph.SQL.SELECT, selectionEdge);
    return ((QueryGraphOperatorProcessingContextImpl) queryGraphProcessingContext)
        .withCurrentRoundComponents(selectionEdge);
  }

  private <X, J, C, K, A>
      SelectionEdge processPluralBasicAttributeVertexAsSelectionEdgeOnFromObjectEdge(
          QueryGraphOperatorProcessingContext queryGraphProcessingContext,
          BasicAttributeVertex basicAttributeVertex,
          FromObjectEdge fromObjectEdge) {
    From<X, J> from = fromObjectEdge.fromObjectSupplier().fromObject();
    if (basicAttributeVertex
        .persistableGraphQlAttribute()
        .jpaAttribute()
        .getDeclaringType()
        .getJavaType()
        .isAssignableFrom(from.getJavaType())) {
      JpaSelectionSupplier jpaSelectionSupplier =
          getSelectionSupplierOfExpressionOnFromObjectForPluralAttributeSupplier(
              from,
              basicAttributeVertex
                  .persistableGraphQlAttribute()
                  .pluralJpaAttributeSupplierIfApt()
                  .get());
      return SelectionEdgeImpl.builder()
          .selectionSupplier(jpaSelectionSupplier)
          .parentPath(basicAttributeVertex.vertexPath().parentPath())
          .childPath(basicAttributeVertex.vertexPath())
          .build();
    }
    // TODO: handle declaring type and from type not compatible
    return null;
  }

  private <X, J, C, A, K>
      JpaSelectionSupplier getSelectionSupplierOfExpressionOnFromObjectForPluralAttributeSupplier(
          From<X, J> fromObject, PluralJpaAttributeSupplier pluralJpaAttributeSupplier) {
    if (pluralJpaAttributeSupplier.asListAttribute().isPresent()) {
      ListAttribute<J, A> attribute =
          pluralJpaAttributeSupplier.<J, List<A>, A>asListAttribute().get();
      return () -> fromObject.get(attribute);
    }
    if (pluralJpaAttributeSupplier.asSetAttribute().isPresent()) {
      SetAttribute<J, A> attribute =
          pluralJpaAttributeSupplier.<J, Set<A>, A>asSetAttribute().get();
      return () -> fromObject.get(attribute);
    }
    if (pluralJpaAttributeSupplier.asMapAttribute().isPresent()) {
      MapAttribute<J, K, A> attribute =
          pluralJpaAttributeSupplier.<J, Map<K, A>, K, A>asMapAttribute().get();
      return () -> fromObject.get(attribute);
    }
    if (pluralJpaAttributeSupplier.asCollectionAttribute().isPresent()) {
      CollectionAttribute<J, A> attribute =
          pluralJpaAttributeSupplier.<J, Collection<A>, A>asCollectionAttribute().get();
      return () -> fromObject.get(attribute);
    }
    // TODO: handle plural attribute not mapped to selection expression
    return null;
  }

  private <X, J, A>
      SelectionEdge processSingularBasicAttributeVertexAsSelectionEdgeOnFromObjectEdge(
          QueryGraphOperatorProcessingContext context,
          BasicAttributeVertex basicAttributeVertex,
          FromObjectEdge fromObjectEdge) {
    From<X, J> from = fromObjectEdge.fromObjectSupplier().fromObject();
    if (basicAttributeVertex
        .persistableGraphQlAttribute()
        .jpaAttribute()
        .getDeclaringType()
        .getJavaType()
        .isAssignableFrom(from.getJavaType())) {
      SingularAttribute<J, A> singularAttribute =
          basicAttributeVertex
              .persistableGraphQlAttribute()
              .singularJpaAttributeSupplierIfApt()
              .get()
              .singularAttribute();
      Path<A> attributeJpaPath = from.get(singularAttribute);
      return SelectionEdgeImpl.builder()
          .parentPath(basicAttributeVertex.vertexPath().parentPath())
          .childPath(basicAttributeVertex.vertexPath())
          .selectionSupplier(() -> attributeJpaPath)
          .build();
    }
    // TODO: handle type not compatible
    return null;
  }

  private void checkAttributeDeclaringTypeAssignableFromJoinFromObjectType(
      Attribute<?, ?> attribute, Join<?, ?> joinFromObject) {
    if (!attribute
        .getDeclaringType()
        .getJavaType()
        .isAssignableFrom(joinFromObject.getJavaType())) {
      // TODO: handle join java type not compatible with basic singular attribute type's parent
      // type
    }
  }

}
