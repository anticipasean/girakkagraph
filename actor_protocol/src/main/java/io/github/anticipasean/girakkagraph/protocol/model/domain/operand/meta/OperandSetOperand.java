package io.github.anticipasean.girakkagraph.protocol.model.domain.operand.meta;

import io.github.anticipasean.girakkagraph.protocol.base.util.valuesupplier.ValueSupplier;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operand.Operand;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operandset.OperandSet;
import java.util.function.Function;

public interface OperandSetOperand<O extends ValueSupplier<OperandSet<?>>> extends Operand<O> {

  @Override
  default Function<O, Class<?>> representedTypeExtractor(){
    return o -> o.get().getClass();
  }
}
