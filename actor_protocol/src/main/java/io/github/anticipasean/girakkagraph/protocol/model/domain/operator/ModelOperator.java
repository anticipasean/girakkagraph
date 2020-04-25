package io.github.anticipasean.girakkagraph.protocol.model.domain.operator;

import io.github.anticipasean.girakkagraph.protocol.model.domain.arity.Arity;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operand.OperandRestriction;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operandset.OperandSet;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operator.Restriction.Type;
import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.filter.OperatorSupplier;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.immutables.criteria.Criteria;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Value.Immutable
@Criteria
@Criteria.Repository
public interface ModelOperator {

  @Criteria.Id
  @Value.Derived
  default String operatorCallName() {
    return operatorSupplier().operator().callName();
  }

  @Value.Derived
  default String operatorName() {
    return operatorSupplier().operator().name();
  }

  OperatorSupplier operatorSupplier();

  default Arity arity() {
    return operatorSupplier().operator().arity();
  }

  //  @Value.Derived -- Does not like the optional signature
  //  default Optional<? extends Class<? extends Filter>> filterTypeIfApt() {
  //    return Arrays.stream(operatorSupplier().operator().getClass().getInterfaces())
  //        .filter(Filter.class::isAssignableFrom)
  //        .map(aClass -> aClass.<Filter>asSubclass(Filter.class))
  //        .findFirst();
  //  }

  @Value.Derived
  default Class<? extends OperandSet> operandSetType() {
    return operatorSupplier().operator().operandSetType();
  }

  @Value.Derived
  default Set<Restriction> fieldOperandTypeRestrictions() {
    return Stream.concat(
            operatorSupplier().operator().generalRestrictions().stream(),
            operatorSupplier().operator().specificRestrictions().stream())
        .filter(
            restriction ->
                restriction.ofType().equals(Restriction.Type.TYPE)
                    && restriction instanceof OperandRestriction
                    && (((OperandRestriction<?>) restriction)
                            .inPosition()
                            .equals(OperandRestriction.Position.FIELD)
                        || ((OperandRestriction<?>) restriction)
                            .inPosition()
                            .equals(OperandRestriction.Position.ALL)))
        .collect(Collectors.toSet());
  }

  @Derived
  default Set<Restriction> functionalParameterTypeRestrictions() {
    return operatorSupplier().operator().functionalParameterRestrictions().stream()
        .filter(
            restriction ->
                restriction instanceof FunctionalParameterRestriction
                    && restriction.ofType().equals(Type.TYPE))
        .collect(Collectors.toSet());
  }

  default boolean operatesOnFieldType(Class<?> fieldType) {
    if (fieldOperandTypeRestrictions().isEmpty()) {
      return true;
    }
    return fieldOperandTypeRestrictions().stream()
        .map(Restriction::parameterAsObject)
        .map(Restriction.Type.TYPE::apply)
        .map(parameter -> (Class<?>) parameter.get())
        .anyMatch(cls -> cls.isAssignableFrom(fieldType));
  }

  default boolean operatesOnFunctionalParameterType(Class<?> functionalParameterType) {
    if (functionalParameterTypeRestrictions().isEmpty()) {
      return false;
    }
    return functionalParameterTypeRestrictions().stream()
        .map(Restriction::parameterAsObject)
        .map(Type.TYPE::apply)
        .map(parameter -> (Class<?>) parameter.get())
        .anyMatch(cls -> cls.isAssignableFrom(functionalParameterType));
  }

  default boolean operatesOnAtLeastOneOfTheseFunctionalParameterTypes(
      Set<Class<?>> functionalParameterTypes) {
    if (functionalParameterTypeRestrictions().isEmpty()) {
      return false;
    }
    Logger logger = LoggerFactory.getLogger(ModelOperator.class);
    boolean result = functionalParameterTypeRestrictions().stream()
        .map(Restriction::parameterAsObject)
        .map(Type.TYPE::apply)
        .map(parameter -> (Class<?>) parameter.get())
        .anyMatch(
            cls ->
                functionalParameterTypes.stream().anyMatch(fType -> cls.isAssignableFrom(fType)));
    logger.info(
        "operates_on_func_param_types: func_param_types: [ "
            + functionalParameterTypes.stream()
            .map(Class::getSimpleName)
            .collect(Collectors.joining(", "))
            + " ]: result: " + result);
    logger.info(
        "operates_on_func_param_types: func_param_restric: "
            + functionalParameterTypeRestrictions().stream()
            .map(Objects::toString)
            .collect(Collectors.joining(", ")));
    return result;
  }

  default boolean operatesOnAtLeastOneOfTheseFieldTypes(Set<Class<?>> fieldTypes) {
    if (fieldOperandTypeRestrictions().isEmpty()) {
      return true;
    }
    return fieldOperandTypeRestrictions().stream()
        .map(Restriction::parameterAsObject)
        .map(Type.TYPE::apply)
        .map(parameter -> (Class<?>) parameter.get())
        .anyMatch(
            cls -> fieldTypes.stream().anyMatch(fieldType -> cls.isAssignableFrom(fieldType)));
  }
}
