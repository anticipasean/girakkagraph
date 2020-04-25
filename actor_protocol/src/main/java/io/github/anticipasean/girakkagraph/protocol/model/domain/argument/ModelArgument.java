package io.github.anticipasean.girakkagraph.protocol.model.domain.argument;

import io.github.anticipasean.girakkagraph.protocol.model.domain.operandset.OperandSet;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operator.Operator;
import org.immutables.value.Value.Derived;

public interface ModelArgument<O extends OperandSet<?>, R> {

  Operator<O, R> operator();

  O operandSet();

  @Derived
  default R operationResult() {
    return operator().operation().apply(operandSet());
  }
}
