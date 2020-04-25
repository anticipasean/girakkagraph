package io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa;

import io.github.anticipasean.girakkagraph.protocol.model.domain.argument.ModelArgument;
import io.github.anticipasean.girakkagraph.protocol.model.domain.argument.jpa.CriteriaJpaFunctionalOrderArgument;
import org.immutables.value.Value.Derived;
import org.immutables.value.Value.Immutable;

@Immutable
public interface FunctionalOrderArgumentEdge extends OrderArgumentEdge {

  CriteriaJpaFunctionalOrderArgument criteriaJpaFunctionalOrderArgument();

  @Override
  @Derived
  default ModelArgument<?, ?> modelArgument(){
    return  criteriaJpaFunctionalOrderArgument();
  }
}
