package io.github.anticipasean.girakkagraph.protocol.model.domain.operator;

import io.github.anticipasean.girakkagraph.protocol.model.domain.arity.ArityRestricted;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operandset.OperandSet;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operation.Operation;
import java.util.Optional;
import java.util.Set;

public interface Operator<O extends OperandSet, R> extends ArityRestricted {

  String name();

  String callName();

  Class<O> operandSetType();

  Set<Restriction<OperandSet>> generalRestrictions();

  Set<Restriction<O>> specificRestrictions();

  Set<Restriction<?>> functionalParameterRestrictions();

  Operation<O, R> operation();

  default Optional<Restriction<O>> findAnySpecificRestrictionsNotMet(O operandSet) {
    return specificRestrictions().stream()
        .filter(oRestriction -> !oRestriction.condition().test(operandSet))
        .findAny();
  }

  default Optional<Restriction<OperandSet>> findAnyGeneralRestrictionsNotMet(OperandSet<?> operandSet) {
    return generalRestrictions().stream()
        .filter(restriction -> !restriction.condition().test(operandSet))
        .findAny();
  }

  default boolean meetsGeneralRestrictions(OperandSet<?> operandSet) {
    return !findAnyGeneralRestrictionsNotMet(operandSet).isPresent();
  }

  default boolean meetsRestrictions(O operandSet) {
    return !findAnyGeneralRestrictionsNotMet(operandSet).isPresent()
        && !findAnySpecificRestrictionsNotMet(operandSet).isPresent();
  }
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
                operandNotMatchingExpectingTypeMessageSupplier.apply(expectedOperandType, operand));
          }
        }
      };

    static final BiFunction<Operator, BiFunction<CriteriaBuilder, List<Operand>, ReturnValue>, ReturnValue>
    includeGeneralChecksBeforeExecution =
   (operator, coreFunc) ->
    checkArityOfOperandArguments.accept(operator.arity(), operator.
    checkOperandsMatchExpectedTypes.accept(expectedOperandTypes, operandList);
    return coreFunc.apply(criteriaBuilder, operandList);
  };

*/
