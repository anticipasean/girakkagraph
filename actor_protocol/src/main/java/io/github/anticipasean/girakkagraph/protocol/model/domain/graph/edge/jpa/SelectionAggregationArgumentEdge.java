package io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa;

import io.github.anticipasean.girakkagraph.protocol.model.domain.argument.ModelArgument;
import io.github.anticipasean.girakkagraph.protocol.model.domain.argument.jpa.CriteriaJpaAggregrationArgument;
import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.ExpressionSupplier;
import org.immutables.value.Value;

@Value.Immutable
public interface SelectionAggregationArgumentEdge extends AggregationArgumentEdge {

  @Value.Derived
  default ExpressionSupplier edgeExpressionSupplier() {
    return aggregationArgument().operationResult();
  }

  CriteriaJpaAggregrationArgument aggregationArgument();

  @Override
  default ModelArgument<?, ?> modelArgument() {
    return aggregationArgument();
  }
}
