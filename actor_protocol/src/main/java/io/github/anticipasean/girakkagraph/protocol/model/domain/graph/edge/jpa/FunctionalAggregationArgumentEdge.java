package io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa;

import io.github.anticipasean.girakkagraph.protocol.model.domain.argument.ModelArgument;
import io.github.anticipasean.girakkagraph.protocol.model.domain.argument.jpa.CriteriaJpaFunctionalAggregationArgument;
import org.immutables.value.Value.Derived;
import org.immutables.value.Value.Immutable;

@Immutable
public interface FunctionalAggregationArgumentEdge extends AggregationArgumentEdge {

  CriteriaJpaFunctionalAggregationArgument criteriaJpaFunctionalAggregationArgument();

  @Derived
  @Override
  default ModelArgument<?, ?> modelArgument(){
    return criteriaJpaFunctionalAggregationArgument();
  }
}
