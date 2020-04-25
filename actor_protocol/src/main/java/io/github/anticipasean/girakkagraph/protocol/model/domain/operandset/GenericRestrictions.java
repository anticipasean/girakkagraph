package io.github.anticipasean.girakkagraph.protocol.model.domain.operandset;

import io.github.anticipasean.girakkagraph.protocol.model.domain.operand.OperandRestriction;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operand.OperandRestrictionImpl;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operator.FunctionalParameterRestrictionImpl;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operator.Restriction;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operator.Restriction.Type;
import java.util.Collection;

public interface GenericRestrictions {

  static <O extends OperandSet<?>> Restriction<O> operandSetMustBeOfType(
      Class<? extends OperandSet<?>> operandSetType) {
    return OperandSetRestrictionImpl.<O>builder()
        .ofType(Restriction.Type.TYPE)
        .condition(operandSetType::isInstance)
        .parameterAsObject(operandSetType)
        .build();
  }

  static <O extends OperandSet<?>> Restriction<O> operandSetNotEmpty() {
    return operandSetMustBeAtLeastSize(1);
  }

  static <O extends OperandSet<?>> Restriction<O> operandSetMustBeSize(int size) {
    return OperandSetRestrictionImpl.<O>builder()
        .ofType(Restriction.Type.SIZE)
        .condition(operandSet -> operandSet.operands().size() == size)
        .parameterAsObject(size)
        .build();
  }

  static <O extends OperandSet<?>> Restriction<O> operandSetMustBeAtLeastSize(int size) {
    return OperandSetRestrictionImpl.<O>builder()
        .ofType(Restriction.Type.SIZE)
        .condition(operandSet -> operandSet.operands().size() >= size)
        .parameterAsObject(size)
        .build();
  }

  static <O extends OperandSet<?>> Restriction<O> fieldOperandMustRepresentType(
      Class<?> representedType) {
    return OperandRestrictionImpl.<O>builder()
        .ofType(Restriction.Type.TYPE)
        .condition(
            o ->
                operandSetMustBeAtLeastSize(1).condition().test(o)
                    && representedType.isAssignableFrom(o.operands().get(0).representedType()))
        .parameterAsObject(representedType)
        .inPosition(OperandRestriction.Position.FIELD)
        .build();
  }

  static <O extends OperandSet<?>> Restriction<O> firstValueOperandMustRepresentType(
      Class<?> representedType) {
    return OperandRestrictionImpl.<O>builder()
        .ofType(Restriction.Type.TYPE)
        .condition(
            o ->
                operandSetMustBeAtLeastSize(2).condition().test(o)
                    && representedType.isAssignableFrom(o.operands().get(1).representedType()))
        .parameterAsObject(representedType)
        .inPosition(OperandRestriction.Position.FIRST_VALUE)
        .build();
  }

  static <O extends OperandSet<?>> Restriction<O> secondValueOperandMustRepresentType(
      Class<?> representedType) {
    return OperandRestrictionImpl.<O>builder()
        .ofType(Restriction.Type.TYPE)
        .condition(
            o ->
                operandSetMustBeAtLeastSize(3).condition().test(o)
                    && representedType.isAssignableFrom(o.operands().get(1).representedType()))
        .parameterAsObject(representedType)
        .inPosition(OperandRestriction.Position.SECOND_VALUE)
        .build();
  }

  static <O extends OperandSet<?>> Restriction<O> fieldOperandMustRepresentString() {
    return fieldOperandMustRepresentType(String.class);
  }

  static <O extends OperandSet<?>> Restriction<O> fieldOperandMustBeNumeric() {
    return fieldOperandMustRepresentType(Number.class);
  }

  static <O extends OperandSet<?>> Restriction<O> fieldOperandMustRepresentComparable() {
    return fieldOperandMustRepresentType(Comparable.class);
  }

  static <O extends OperandSet<?>> Restriction<O> fieldOperandMustRepresentCollection() {
    return fieldOperandMustRepresentType(Collection.class);
  }

  static <O extends OperandSet<?>> Restriction<O> allOperandsMustRepresentType(
      Class<?> representedType) {
    return OperandRestrictionImpl.<O>builder()
        .ofType(Restriction.Type.TYPE)
        .condition(
            operandSet ->
                operandSet.operands().stream()
                    .allMatch(o -> representedType.isAssignableFrom(o.getClass())))
        .parameterAsObject(representedType)
        .inPosition(OperandRestriction.Position.ALL)
        .build();
  }

  static <O extends OperandSet<?>> Restriction<O> allOperandsMustRepresentTypeString() {
    return allOperandsMustRepresentType(String.class);
  }

  static <O extends OperandSet<?>> Restriction<O> allOperandsMustRepresentTypeNumber() {
    return allOperandsMustRepresentType(Number.class);
  }

  static <O extends OperandSet<?>> Restriction<O> allOperandsMustRepresentTypeComparable() {
    return allOperandsMustRepresentType(Comparable.class);
  }

  static <O extends OperandSet<?>> Restriction<O> allOperandsMustRepresentTypeCollection() {
    return allOperandsMustRepresentType(Collection.class);
  }

  static <T> Restriction<T> functionalParameterIsOfType(Class<T> restrictedType) {
    return FunctionalParameterRestrictionImpl.<T>builder()
        .ofType(Type.TYPE)
        .condition(o -> restrictedType.isAssignableFrom(o.getClass()))
        .parameterAsObject(restrictedType)
        .build();
  }
  static Restriction<String> functionalParameterIsOfTypeString() {
    return functionalParameterIsOfType(String.class);
  }
}
