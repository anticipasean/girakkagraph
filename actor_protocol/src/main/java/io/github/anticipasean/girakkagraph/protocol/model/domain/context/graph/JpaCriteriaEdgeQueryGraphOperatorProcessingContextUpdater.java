package io.github.anticipasean.girakkagraph.protocol.model.domain.context.graph;

import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.QueryModelGraph.SQL;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.AggregationArgumentEdge;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.AggregationPredicateArgumentEdge;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.FromObjectEdge;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.JoinEdge;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.JpaCriteriaEdge;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.OrderArgumentEdge;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.RootFromVertex;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.SelectionAggregationArgumentEdge;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.SelectionPredicateArgumentEdge;
import io.github.anticipasean.girakkagraph.typematcher.TypeMatcher;
import java.util.function.BiFunction;
import java.util.function.Consumer;

interface JpaCriteriaEdgeQueryGraphOperatorProcessingContextUpdater
    extends BiFunction<
        QueryGraphOperatorProcessingContext, JpaCriteriaEdge, QueryGraphOperatorProcessingContext> {

  static JpaCriteriaEdgeQueryGraphOperatorProcessingContextUpdater getInstance(){
    return Updater.INSTANCE.jpaCriteriaEdgeQueryGraphOperatorProcessingContextUpdater;
  }

  static <T extends JpaCriteriaEdge> Consumer<T> insertIntoSQLComponentMapUnder(
      SQL key, QueryGraphOperatorProcessingContext queryGraphOperatorProcessingContext) {
    return (jpaCriteriaEdge) -> {
      queryGraphOperatorProcessingContext
          .queryCriteriaEdgesProcessingMap()
          .putEdge(key, jpaCriteriaEdge);
    };
  }

  @Override
  default QueryGraphOperatorProcessingContext apply(
      QueryGraphOperatorProcessingContext queryGraphOperatorProcessingContext,
      JpaCriteriaEdge jpaCriteriaEdge) {
    TypeMatcher.whenTypeOf(jpaCriteriaEdge)
        .is(AggregationArgumentEdge.class)
        .then(insertIntoSQLComponentMapUnder(SQL.GROUP_BY, queryGraphOperatorProcessingContext))
        .is(SelectionPredicateArgumentEdge.class)
        .then(insertIntoSQLComponentMapUnder(SQL.WHERE, queryGraphOperatorProcessingContext))
        .is(OrderArgumentEdge.class)
        .then(insertIntoSQLComponentMapUnder(SQL.ORDER_BY, queryGraphOperatorProcessingContext))
        .is(SelectionAggregationArgumentEdge.class)
        .then(insertIntoSQLComponentMapUnder(SQL.SELECT, queryGraphOperatorProcessingContext))
        .is(AggregationPredicateArgumentEdge.class)
        .then(insertIntoSQLComponentMapUnder(SQL.HAVING, queryGraphOperatorProcessingContext))
        .is(RootFromVertex.class)
        .then(insertIntoSQLComponentMapUnder(SQL.ROOT, queryGraphOperatorProcessingContext))
        .is(JoinEdge.class)
        .then(insertIntoSQLComponentMapUnder(SQL.JOIN, queryGraphOperatorProcessingContext))
        .is(FromObjectEdge.class)
        .then(insertIntoSQLComponentMapUnder(SQL.FROM, queryGraphOperatorProcessingContext))
        .orElseThrow(
            () ->
                new UnsupportedOperationException(
                    String.format(
                        "the jpa criteria argument edge [ %s ] is not supported for mapping to the "
                            + "query criteria edges processing context",
                        jpaCriteriaEdge)));
    return queryGraphOperatorProcessingContext;
  }

  enum Updater {
    INSTANCE(new JpaCriteriaEdgeQueryGraphOperatorProcessingContextUpdater() {});
    private final JpaCriteriaEdgeQueryGraphOperatorProcessingContextUpdater
        jpaCriteriaEdgeQueryGraphOperatorProcessingContextUpdater;
    Updater(
        JpaCriteriaEdgeQueryGraphOperatorProcessingContextUpdater
            jpaCriteriaEdgeQueryGraphOperatorProcessingContextUpdater) {
      this.jpaCriteriaEdgeQueryGraphOperatorProcessingContextUpdater =
          jpaCriteriaEdgeQueryGraphOperatorProcessingContextUpdater;
    }
  }
}
