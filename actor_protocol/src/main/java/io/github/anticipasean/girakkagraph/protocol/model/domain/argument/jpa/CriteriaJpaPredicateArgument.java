package io.github.anticipasean.girakkagraph.protocol.model.domain.argument.jpa;

import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.PredicateSupplier;
import org.immutables.value.Value;

@Value.Immutable
public interface CriteriaJpaPredicateArgument extends CriteriaJpaArgument<PredicateSupplier> {}
