package io.github.anticipasean.girakkagraph.protocol.model.domain.operand;

import io.github.anticipasean.girakkagraph.protocol.base.util.valuesupplier.ValueSupplier;
import java.util.function.Function;

/**
 * Participant in an operation, member of an {@link
 * io.github.anticipasean.girakkagraph.protocol.model.domain.operandset.OperandSet} A value or container
 * thereof that may be passed into the operation of an operator
 */
public interface Operand<V extends ValueSupplier<?>> {

  V valueSupplier();

  Function<V, Class<?>> representedTypeExtractor();

  default Class<?> representedType() {
    return representedTypeExtractor().apply(valueSupplier());
  }
}
