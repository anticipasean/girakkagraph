package io.github.anticipasean.girakkagraph.protocol.model.domain.operand.meta;

import io.github.anticipasean.girakkagraph.protocol.base.util.valuesupplier.ValueSupplier;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operand.Operand;
import java.util.function.Function;
import java.util.function.Supplier;

public interface FunctionalOperand<F extends Function<T, R> & ValueSupplier<R>, T, R>
    extends Operand<F> {

  @SuppressWarnings("unchecked")
  static <T, R, F extends Function<T, R> & ValueSupplier<R>> FunctionalOperand<F, T, R> newOperand(
      Function<T, R> functionalValue, Supplier<Class<?>> representedTypeSupplier) {
    return new FunctionalOperand<F, T, R>() {
      @Override
      public F functionalValue() {
        return (F) functionalValue;
      }

      @Override
      public Function<F, Class<?>> representedTypeExtractor() {
        return f -> representedTypeSupplier.get();
      }
    };
  }

  F functionalValue();

  @Override
  default F valueSupplier() {
    return functionalValue();
  }
}
