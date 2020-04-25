package io.github.anticipasean.girakkagraph.protocol.model.domain.graph;

import akka.japi.Pair;
import com.google.common.collect.ImmutableMap;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.ModelEdge.EdgeKey;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.AggregationArgumentEdge;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.AggregationPredicateArgumentEdge;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.FromObjectEdge;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.JoinEdge;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.OrderArgumentEdge;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.RootFromVertex;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.SelectionEdge;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.SelectionPredicateArgumentEdge;
import java.util.Arrays;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.immutables.value.Value;

@Value.Immutable
public interface QueryModelGraph extends ModelGraph {

  CriteriaQuery<Tuple> tupleCriteriaQuery();

  CriteriaBuilder criteriaBuilder();

  Optional<RootFromVertex> rootFromVertexIfAvail();

  Map<SQL, NavigableMap<EdgeKey, ModelEdge>> sqlComponentToEdgeMappings();



//  @Value.Lazy
//  default Map<SQL, List<ModelEdge>> queryComponentEdges() {
//    Map<SQL, List<ModelEdge>> sqlModelEdgeImmutableListMap =
//        edges().stream()
//            .map(
//                modelEdge ->
//                    Pair.create(
//                        SQL.modelEdgeClassMaybeAssignableToSQLComponentType(modelEdge.getClass()),
//                        modelEdge))
//            .filter(optionalModelEdgePair -> optionalModelEdgePair.first().isPresent())
//            .map(
//                optionalModelEdgePair ->
//                    Pair.create(
//                        optionalModelEdgePair.first().get(), optionalModelEdgePair.second()))
//            .collect(
//                Collectors.groupingByConcurrent(
//                    Pair::first,
//                    Collectors.collectingAndThen(
//                        Collectors.toList(),
//                        pairs ->
//                            pairs.stream()
//                                .map(Pair::second)
//                                .reduce(
//                                    ImmutableList.<ModelEdge>builder(),
//                                    ImmutableList.Builder::add,
//                                    (modelEdgeBuilder, modelEdgeBuilder2) -> modelEdgeBuilder2)
//                                .build())));
//    return ImmutableMap.<SQL, List<ModelEdge>>builder().putAll(sqlModelEdgeImmutableListMap).build();
//  }

  enum SQL {
    ROOT(Root.class, RootFromVertex.class),
    JOIN(Join.class, JoinEdge.class),
    FROM(From.class, FromObjectEdge.class),
    WHERE(Predicate.class, SelectionPredicateArgumentEdge.class),
    GROUP_BY(Expression.class, AggregationArgumentEdge.class),
    HAVING(Expression.class, AggregationPredicateArgumentEdge.class),
    SELECT(Expression.class, SelectionEdge.class),
    ORDER_BY(Expression.class, OrderArgumentEdge.class);
    private static final Map<? extends Class<? extends ModelEdge>, SQL> edgeTypeToSQL =
        Arrays.stream(SQL.values())
            .map(sql -> Pair.create(sql.modelEdgeType, sql))
            .reduce(
                ImmutableMap.<Class<? extends ModelEdge>, SQL>builder(),
                (classSQLBuilder, classSQLPair) ->
                    classSQLBuilder.put(classSQLPair.first(), classSQLPair.second()),
                (classSQLBuilder, classSQLBuilder2) -> classSQLBuilder2)
            .build();
    private final Class<? extends Expression> jpaCriteriaExpressionType;
    private final Class<? extends ModelEdge> modelEdgeType;

    SQL(
        Class<? extends Expression> jpaCriteriaExpressionType,
        Class<? extends ModelEdge> modelEdgeType) {
      this.jpaCriteriaExpressionType = jpaCriteriaExpressionType;
      this.modelEdgeType = modelEdgeType;
    }

    public static Optional<SQL> modelEdgeClassMaybeAssignableToSQLComponentType(
        Class<? extends ModelEdge> modelEdgeClass) {
      return Arrays.stream(SQL.values())
          .filter(sql -> sql.modelEdgeType.isAssignableFrom(modelEdgeClass))
          .findAny();
    }

    public static Map<? extends Class<? extends ModelEdge>, SQL> edgeTypeToSQL() {
      return edgeTypeToSQL;
    }

    public Class<? extends Expression> getJpaCriteriaExpressionType() {
      return jpaCriteriaExpressionType;
    }

    public Class<? extends ModelEdge> getModelEdgeType() {
      return modelEdgeType;
    }
  }
}
