package io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa;

import io.github.anticipasean.girakkagraph.protocol.model.domain.argument.ModelArgument;
import io.github.anticipasean.girakkagraph.protocol.model.domain.argument.jpa.CriteriaJpaOrderArgument;
import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.OrderSupplier;
import org.immutables.value.Value;

@Value.Immutable
public interface BasicOrderArgumentEdge extends OrderArgumentEdge {

  @Value.Derived
  default OrderSupplier edgeOrderSupplier() {
    return criteriaJpaOrderArgument().operationResult();
  }

  CriteriaJpaOrderArgument criteriaJpaOrderArgument();

  @Override
  default ModelArgument<?, ?> modelArgument() {
    return criteriaJpaOrderArgument();
  }
}
