package io.github.anticipasean.girakkagraph.protocol.model.domain.context.graph;

import io.github.anticipasean.girakkagraph.protocol.model.domain.context.ProcessingContext;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.ModelGraph;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.ModelGraphComponent;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.JpaCriteriaEdge;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.extrapolator.query.QueryCriteriaEdgesProcessingMap;
import java.util.Set;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(
    overshadowImplementation = true,
    typeImmutable = "*Impl",
    depluralize = true,
    depluralizeDictionary = {"currentRoundComponent:currentRoundComponents"})
public interface QueryGraphOperatorProcessingContext extends ProcessingContext {

  CriteriaBuilder criteriaBuilder();

  CriteriaQuery<Tuple> criteriaQuery();

  ModelGraph baseModelGraph();

  QueryCriteriaEdgesProcessingMap queryCriteriaEdgesProcessingMap();

  Set<ModelGraphComponent> currentRoundComponents();

  default QueryGraphOperatorProcessingContext sameContextButNoComponents() {
    return QueryGraphOperatorProcessingContextImpl.builder()
        .criteriaQuery(criteriaQuery())
        .criteriaBuilder(criteriaBuilder())
        .queryCriteriaEdgesProcessingMap(queryCriteriaEdgesProcessingMap())
        .baseModelGraph(baseModelGraph())
        .build();
  }

  default QueryGraphOperatorProcessingContext updateWithJpaCriteriaEdge(
      JpaCriteriaEdge jpaCriteriaEdge) {
    return JpaCriteriaEdgeQueryGraphOperatorProcessingContextUpdater.getInstance()
        .apply(this, jpaCriteriaEdge);
  }
}
