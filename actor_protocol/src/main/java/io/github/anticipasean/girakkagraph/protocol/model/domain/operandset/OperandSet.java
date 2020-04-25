package io.github.anticipasean.girakkagraph.protocol.model.domain.operandset;

import io.github.anticipasean.girakkagraph.protocol.model.domain.operand.Operand;
import java.util.List;
import org.immutables.value.Value;

@Value.Style(
    overshadowImplementation = true,
    typeImmutable = "*Impl",
    depluralize = true,
    depluralizeDictionary = {"operand:operands"})
public interface OperandSet<O extends Operand> {

  List<O> operands();
}

/*
      BiFunction<Class<? extends Operand>, Operand, String>
          operandNotMatchingExpectingTypeMessageSupplier =
              (expectedOperandClass, operand) ->
                  String.format(
                      "the operand [ %s ] does not match the expected type: [ %s ]",
                      operand.toString(), expectedOperandClass.getName());
      BiConsumer<List<Class<? extends Operand>>, List<Operand>> checkOperandsMatchExpectedTypes =
          (operandTypesExpected, operandList) -> {
            Iterator<Class<? extends Operand>> expectedOperandTypeIterator =
                operandTypesExpected.iterator();
            Iterator<Operand> operandIterator = operandList.iterator();
            while (expectedOperandTypeIterator.hasNext() && operandIterator.hasNext()) {
              Class<? extends Operand> expectedOperandType = expectedOperandTypeIterator.next();
              Operand operand = operandIterator.next();
              if (!expectedOperandType.isAssignableFrom(operand.getClass())) {
                throw new IllegalArgumentException(
                    operandNotMatchingExpectingTypeMessageSupplier.apply(
                        expectedOperandType, operand));
              }
            }
          };

            @Value.Check
  default void checkArityOfOperands() {
    if (!arity().matchesArity(operands())) {
      Supplier<String> messageSupplier =
          () ->
              String.format(
                  "the number of operands [ %d ] for this set does not match its arity [ %d: %s] ",
                  operands().size(), arity().operandCount(), arity().name());
      throw new IllegalArgumentException(messageSupplier.get());
    }
  }
* */
