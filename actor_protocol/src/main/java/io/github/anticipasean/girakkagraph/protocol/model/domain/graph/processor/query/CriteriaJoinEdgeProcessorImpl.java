package io.github.anticipasean.girakkagraph.protocol.model.domain.graph.processor.query;

import io.github.anticipasean.girakkagraph.protocol.model.domain.context.graph.QueryGraphOperatorProcessingContext;
import io.github.anticipasean.girakkagraph.protocol.model.domain.context.graph.QueryGraphOperatorProcessingContextImpl;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.EdgeKeyImpl;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.ModelEdge;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.ModelEdge.EdgeKey;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.QueryModelGraph;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.BranchJoinEdge;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.BranchJoinEdgeImpl;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.JoinEdge;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.RootFromVertex;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.RootFromVertexImpl;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.RootJoinEdge;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.RootJoinEdgeImpl;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.vertex.JunctionVertex;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.vertex.RootVertex;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.vertex.TypeVertex;
import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.JpaEntityTypeSupplier;
import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.PluralJpaAttributeSupplier;
import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.RootFromExpressionSupplier;
import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.SingularJpaAttributeSupplier;
import io.github.anticipasean.girakkagraph.typematcher.TypeMatcher;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Supplier;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CriteriaJoinEdgeProcessorImpl implements CriteriaJoinEdgeProcessor {
  private final Logger logger;

  public CriteriaJoinEdgeProcessorImpl() {
    this.logger = LoggerFactory.getLogger(CriteriaJoinEdgeProcessorImpl.class);
  }

  @Override
  public QueryGraphOperatorProcessingContext updateContextAccordingToComponent(
      QueryGraphOperatorProcessingContext context, TypeVertex component) {
    return TypeMatcher.whenTypeOf(component)
        .is(RootVertex.class)
        .thenApply(rootVertex -> createQueryRootFromContextAndRootVertex(context, rootVertex))
        .is(JunctionVertex.class)
        .thenApply(
            junctionVertex ->
                createRootOrBranchJoinFromContextAndJunctionVertex(context, junctionVertex))
        .orElse(context.sameContextButNoComponents());
  }

  private <X> QueryGraphOperatorProcessingContext createQueryRootFromContextAndRootVertex(
      QueryGraphOperatorProcessingContext queryGraphProcessingContext, RootVertex rootVertex) {
    if (!rootVertex.persistableGraphQlType().isEntity()) {
      // TODO: handle root not entity
    }
    logger.info("creating root from root vertex: " + rootVertex.vertexPath().uri().toString());
    JpaEntityTypeSupplier jpaEntityTypeSupplier =
        rootVertex.persistableGraphQlType().jpaEntitySupplierIfApt().get();
    Root<X> rootFromObject =
        queryGraphProcessingContext.criteriaQuery().from(jpaEntityTypeSupplier.jpaEntityType());
    RootFromExpressionSupplier rootFromExpressionSupplier = () -> rootFromObject;
    RootFromVertex rootFromVertex =
        RootFromVertexImpl.builder()
            .from(rootVertex)
            .rootFromExpressionSupplier(rootFromExpressionSupplier)
            .build();
    queryGraphProcessingContext
        .queryCriteriaEdgesProcessingMap()
        .putEdge(QueryModelGraph.SQL.ROOT, rootFromVertex);
    return ((QueryGraphOperatorProcessingContextImpl) queryGraphProcessingContext)
        .withCurrentRoundComponents(rootFromVertex);
  }

  /*private void logCurrentMapState(QueryCriteriaEdgesProcessingMap queryCriteriaEdgesProcessingMap, ModelPath path) {
    String mapContent = queryCriteriaEdgesProcessingMap.entrySet().stream()
        .map(
            sqlConcurrentNavigableMapEntry ->
                String.join(
                    ": ",
                    sqlConcurrentNavigableMapEntry.getKey().name(),
                    sqlConcurrentNavigableMapEntry.getValue().entrySet()
                        .stream()
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
                                            .map(
                                                cls ->
                                                    cls.getSimpleName())
                                            .collect(
                                                Collectors.joining(
                                                    ",\n\t\t")))
                                    .append(" ]")
                                    .toString())
                        .collect(Collectors.joining(",\n\t")))
                    + "\n\t")
        .collect(Collectors.joining("\n\t"));
    LoggerFactory.getLogger(QueryCriteriaEdgesProcessingMap.class).info(String.format("path: %s\nquery_criteria_edges_processing_map: \n", path) + mapContent);
  }*/

  private QueryGraphOperatorProcessingContext createRootOrBranchJoinFromContextAndJunctionVertex(
      QueryGraphOperatorProcessingContext queryGraphProcessingContext,
      JunctionVertex junctionVertex) {
    Optional<RootFromVertex> rootFromVertex =
        queryGraphProcessingContext.queryCriteriaEdgesProcessingMap().getRootIfAdded();
    if (rootFromVertex.isPresent()
        && junctionVertex.vertexPath().parentPath().equals(rootFromVertex.get().vertexPath())) {
      logger.info("creating root join edge from: " + junctionVertex.vertexPath().uri());
      RootJoinEdge rootJoinEdge = createRootJoinEdge(queryGraphProcessingContext, junctionVertex);
      queryGraphProcessingContext
          .queryCriteriaEdgesProcessingMap()
          .putEdge(QueryModelGraph.SQL.JOIN, rootJoinEdge);
      return ((QueryGraphOperatorProcessingContextImpl) queryGraphProcessingContext)
          .withCurrentRoundComponents(rootJoinEdge, junctionVertex);
    } else {
      logger.info("creating branch join edge from: " + junctionVertex.vertexPath().uri());
      BranchJoinEdge branchJoinEdge =
          createBranchJoinEdge(queryGraphProcessingContext, junctionVertex);
      queryGraphProcessingContext
          .queryCriteriaEdgesProcessingMap()
          .putEdge(QueryModelGraph.SQL.JOIN, branchJoinEdge);
      return ((QueryGraphOperatorProcessingContextImpl) queryGraphProcessingContext)
          .withCurrentRoundComponents(branchJoinEdge, junctionVertex);
    }
  }

  private <R, J> RootJoinEdge createRootJoinEdge(
      QueryGraphOperatorProcessingContext queryGraphProcessingContext,
      JunctionVertex junctionVertex) {
    Optional<RootFromVertex> rootFromVertexMaybe =
        queryGraphProcessingContext.queryCriteriaEdgesProcessingMap().getRootIfAdded();
    if (!rootFromVertexMaybe.isPresent()) {
      // TODO: handle root not added
      throw new IllegalStateException("root not yet added");
    }
    RootFromVertex rootFromVertex = rootFromVertexMaybe.get();
    Root<R> rootFromObject = rootFromVertex.rootFromExpressionSupplier().rootFromObject();
    checkParentFromObjectTypeMatchesJoiningAttributeExpectedParentType(
        rootFromObject, junctionVertex.persistableGraphQlAttribute().jpaAttribute());
    if (junctionVertex.persistableGraphQlAttribute().isSingular()) {
      return (RootJoinEdge)
          createJoinEdgeOnParentFromObjectAndSingularAttributeJunctionVertex(
              junctionVertex, rootFromObject);
    } else {
      return (RootJoinEdge)
          createJoinEdgeOnParentFromObjectAndPluralAttributeJunctionVertexWithJoinType(
              junctionVertex, rootFromObject);
    }
  }

  private <X, P, C> JoinEdge createJoinEdgeOnParentFromObjectAndSingularAttributeJunctionVertex(
      JunctionVertex junctionVertex, From<X, P> fromObject) {
    SingularJpaAttributeSupplier singularJpaAttributeSupplier =
        junctionVertex
            .persistableGraphQlAttribute()
            .singularJpaAttributeSupplierIfApt()
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        String.format(
                            "junction vertex attribute should be singular and have jpa singular "
                                + "attribute value within the holder [ %s ]",
                            junctionVertex.vertexPath().uri().toString())));
    SingularAttribute<P, C> singularAttribute = singularJpaAttributeSupplier.singularAttribute();
    if (fromObject instanceof Root) {
      Join<P, C> joinOnSingularAttribute = fromObject.join(singularAttribute, JoinType.INNER);
      return RootJoinEdgeImpl.builder()
          .parentPath(junctionVertex.vertexPath().parentPath())
          .childPath(junctionVertex.vertexPath())
          .rootJoinSupplier(() -> joinOnSingularAttribute)
          .joinType(JoinType.INNER)
          .build();
    } else if (fromObject instanceof Join) {
      Join<P, C> joinOnSingularAttribute = fromObject.join(singularAttribute, JoinType.INNER);
      return BranchJoinEdgeImpl.builder()
          .parentPath(junctionVertex.vertexPath().parentPath())
          .childPath(junctionVertex.vertexPath())
          .branchJoinSupplier(() -> joinOnSingularAttribute)
          .joinType(JoinType.INNER)
          .build();
    } else {
      // TODO: handle from object not recognized type of join --> fetch
      return null;
    }
  }

  private <X, J, C, A, K>
      JoinEdge createJoinEdgeOnParentFromObjectAndPluralAttributeJunctionVertexWithJoinType(
          JunctionVertex junctionVertex, From<X, J> fromObject) {
    PluralJpaAttributeSupplier pluralJpaAttributeSupplier =
        junctionVertex
            .persistableGraphQlAttribute()
            .pluralJpaAttributeSupplierIfApt()
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        String.format(
                            "junction vertex attribute should be plural and have jpa "
                                + "plural attribute value within the holder [ %s ]",
                            junctionVertex.vertexPath().uri().toString())));
    Join<J, A> joinObject = null;
    if (pluralJpaAttributeSupplier.asListAttribute().isPresent()) {
      ListAttribute<J, A> listAttribute =
          pluralJpaAttributeSupplier.<J, List<A>, A>asListAttribute().get();
      joinObject = fromObject.join(listAttribute, JoinType.INNER);
    } else if (pluralJpaAttributeSupplier.asSetAttribute().isPresent()) {
      SetAttribute<J, A> setAttribute =
          pluralJpaAttributeSupplier.<J, Set<A>, A>asSetAttribute().get();
      joinObject = fromObject.join(setAttribute, JoinType.INNER);
    } else if (pluralJpaAttributeSupplier.asMapAttribute().isPresent()) {
      MapAttribute<J, K, A> mapAttribute =
          pluralJpaAttributeSupplier.<J, Map<K, A>, K, A>asMapAttribute().get();
      joinObject = fromObject.join(mapAttribute, JoinType.INNER);
    } else if (pluralJpaAttributeSupplier.asCollectionAttribute().isPresent()) {
      CollectionAttribute<J, A> collectionAttribute =
          pluralJpaAttributeSupplier.<J, Collection<A>, A>asCollectionAttribute().get();
    } else {
      throw new UnsupportedOperationException(
          String.format(
              "attribute type for junction vertex does not have a supported "
                  + "criteria api mapping in this join edge processor: [ %s ]",
              junctionVertex));
    }

    if (fromObject instanceof Root) {
      Join<J, A> finalJoinObject = joinObject;
      return RootJoinEdgeImpl.builder()
          .parentPath(junctionVertex.vertexPath().parentPath())
          .childPath(junctionVertex.vertexPath())
          .rootJoinSupplier(() -> finalJoinObject)
          .joinType(JoinType.INNER)
          .build();
    } else if (fromObject instanceof Join) {
      Join<J, A> finalJoinObject1 = joinObject;
      return BranchJoinEdgeImpl.builder()
          .parentPath(junctionVertex.vertexPath().parentPath())
          .childPath(junctionVertex.vertexPath())
          .branchJoinSupplier(() -> finalJoinObject1)
          .joinType(JoinType.INNER)
          .build();
    } else {
      logger.error("this type of join is not supported");
      return null;
    }
  }

  private BranchJoinEdge createBranchJoinEdge(
      QueryGraphOperatorProcessingContext queryGraphProcessingContext,
      JunctionVertex junctionVertex) {
    if (junctionVertex.vertexPath().depth() < 3) {
      logger.error("junction vertex is not a branch join: " + junctionVertex.vertexPath());
    }
    NavigableMap<EdgeKey, ModelEdge> joinEdges =
        queryGraphProcessingContext
            .queryCriteriaEdgesProcessingMap()
            .getOrDefault(QueryModelGraph.SQL.JOIN, new ConcurrentSkipListMap<>());
    if (!joinEdges.containsKey(
        EdgeKeyImpl.of(
            junctionVertex.vertexPath().parentPath().parentPath(),
            junctionVertex.vertexPath().parentPath()))) {
      logger.error(
          "parent join edge not found: "
              + EdgeKeyImpl.of(
                  junctionVertex.vertexPath().parentPath().parentPath(),
                  junctionVertex.vertexPath().parentPath()));
    }
    ModelEdge modelEdge =
        joinEdges.get(
            EdgeKeyImpl.of(
                junctionVertex.vertexPath().parentPath().parentPath(),
                junctionVertex.vertexPath().parentPath()));
    if (!(modelEdge instanceof JoinEdge)) {
      logger.error(
          "join edge received not actually a join edge: expected: "
              + EdgeKeyImpl.of(
                  junctionVertex.vertexPath().parentPath().parentPath(),
                  junctionVertex.vertexPath().parentPath())
              + " received: "
              + modelEdge);
    }
    if (modelEdge instanceof RootJoinEdge) {
      checkParentFromObjectTypeMatchesJoiningAttributeExpectedParentType(
          ((RootJoinEdge) modelEdge).rootJoinSupplier().joinObject(),
          junctionVertex.persistableGraphQlAttribute().jpaAttribute());
      if (junctionVertex.persistableGraphQlAttribute().isSingular()) {
        return (BranchJoinEdge)
            createJoinEdgeOnParentFromObjectAndSingularAttributeJunctionVertex(
                junctionVertex, ((RootJoinEdge) modelEdge).rootJoinSupplier().joinObject());
      } else {
        return (BranchJoinEdge)
            createJoinEdgeOnParentFromObjectAndPluralAttributeJunctionVertexWithJoinType(
                junctionVertex, ((RootJoinEdge) modelEdge).rootJoinSupplier().joinObject());
      }
    } else if (modelEdge instanceof BranchJoinEdge) {
      checkParentFromObjectTypeMatchesJoiningAttributeExpectedParentType(
          ((BranchJoinEdge) modelEdge).branchJoinSupplier().joinObject(),
          junctionVertex.persistableGraphQlAttribute().jpaAttribute());
      if (junctionVertex.persistableGraphQlAttribute().isSingular()) {
        return (BranchJoinEdge)
            createJoinEdgeOnParentFromObjectAndSingularAttributeJunctionVertex(
                junctionVertex, ((BranchJoinEdge) modelEdge).branchJoinSupplier().joinObject());
      } else {
        return (BranchJoinEdge)
            createJoinEdgeOnParentFromObjectAndPluralAttributeJunctionVertexWithJoinType(
                junctionVertex, ((BranchJoinEdge) modelEdge).branchJoinSupplier().joinObject());
      }
    } else {
      throw new UnsupportedOperationException(
          String.format(
              "the type of join edge this junction vertex represents has not been implemented "
                  + "in this processor [ %s ]",
              junctionVertex));
    }
  }

  private void checkParentFromObjectTypeMatchesJoiningAttributeExpectedParentType(
      From<?, ?> fromObject, Attribute<?, ?> attribute) {
    if (!attribute.getDeclaringType().getJavaType().isAssignableFrom(fromObject.getJavaType())) {
      Supplier<String> messageSupplier =
          () ->
              String.format(
                  "from object [ java_type_name : %s ] does not match the expected parent type of "
                      + "attribute [ name: %s, declaring/managed_type_parent_java_type_name: %s, is_plural: %s ]",
                  fromObject.getJavaType().getName(),
                  attribute.getName(),
                  attribute.getDeclaringType().getJavaType().getName(),
                  attribute.isCollection());
      IllegalStateException exception = new IllegalStateException(messageSupplier.get());
      logger.error(messageSupplier.get(), exception);
      throw exception;
    }
  }
}
