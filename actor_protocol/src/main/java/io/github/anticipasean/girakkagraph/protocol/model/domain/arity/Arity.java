package io.github.anticipasean.girakkagraph.protocol.model.domain.arity;

import com.google.common.collect.ImmutableMap;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operand.Operand;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public enum Arity {
  NULLARY(0),
  UNARY(1),
  BINARY(2),
  TERNARY(3),
  QUATERNARY(4);
  private static Map<Integer, Arity> operandCountToArityMap =
      Arrays.stream(Arity.values())
          .reduce(
              ImmutableMap.<Integer, Arity>builder(),
              (integerArityBuilder, arity) -> integerArityBuilder.put(arity.operandCount, arity),
              (builder, builder2) -> builder2)
          .build();
  private final int operandCount;

  Arity(int operandCount) {
    this.operandCount = operandCount;
  }

  public static Map<Integer, Arity> operandCountToArityMap() {
    return operandCountToArityMap;
  }

  public boolean matchesArity(List<Operand> operandList) {
    return this.operandCount == operandList.size();
  }

  public int operandCount() {
    return operandCount;
  }
}
