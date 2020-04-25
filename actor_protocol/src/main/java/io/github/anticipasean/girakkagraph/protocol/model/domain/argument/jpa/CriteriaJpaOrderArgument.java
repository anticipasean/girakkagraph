package io.github.anticipasean.girakkagraph.protocol.model.domain.argument.jpa;

import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.OrderSupplier;
import org.immutables.value.Value;

@Value.Immutable
public interface CriteriaJpaOrderArgument extends CriteriaJpaArgument<OrderSupplier> {}
