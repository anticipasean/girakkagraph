package io.github.anticipasean.girakkagraph.protocol.model.domain.operand.jpa;

import io.github.anticipasean.girakkagraph.protocol.model.domain.operand.Operand;
import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.ExpressionSupplier;
import java.util.function.Function;
import org.immutables.value.Value;

@Value.Immutable
public interface JpaExpressionOperand extends Operand<ExpressionSupplier> {

  @Override
  default Function<ExpressionSupplier, Class<?>> representedTypeExtractor() {
    return expressionSupplier -> expressionSupplier.expression().getJavaType();
  }
}
