package io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa;

import io.github.anticipasean.girakkagraph.protocol.model.domain.argument.ModelArgument;
import io.github.anticipasean.girakkagraph.protocol.model.domain.argument.jpa.CriteriaJpaPredicateArgument;
import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.PredicateSupplier;
import org.immutables.value.Value;

@Value.Immutable
public interface AggregationPredicateArgumentEdge extends JpaCriteriaArgumentEdge {

  @Value.Derived
  default PredicateSupplier edgePredicateSupplier() {
    return criteriaJpaArgument().operationResult();
  }

  CriteriaJpaPredicateArgument criteriaJpaArgument();

  @Override
  default ModelArgument<?, ?> modelArgument() {
    return criteriaJpaArgument();
  }
}
