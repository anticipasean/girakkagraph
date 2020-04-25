package io.github.anticipasean.girakkagraph.protocol.model.domain.argument.jpa;

import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.ExpressionSupplier;
import org.immutables.value.Value;

@Value.Immutable
public interface CriteriaJpaAggregrationArgument extends CriteriaJpaArgument<ExpressionSupplier> {}
