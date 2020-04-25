package io.github.anticipasean.girakkagraph.protocol.model.domain.operator;

import io.github.anticipasean.girakkagraph.protocol.model.domain.context.ProcessingContext;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.immutables.value.Value;

public interface Restriction<T> {

  Unit onUnit();

  Type ofType();

  Predicate<T> condition();

  String description();

  Object parameterAsObject();

  @Value.Check
  default void checkParameterAsObjectMatchesRestrictionTypeParameterType() {
    Function<Class<?>, String> messageCreator =
        cls -> String.format("parameter of type restriction must be %s", cls.getSimpleName());
    if (!ofType().parameterType().isAssignableFrom(parameterAsObject().getClass())) {
      throw new IllegalArgumentException(messageCreator.apply(ofType().parameterType()));
    }
  }

  enum Unit {
    OPERAND_SET,
    OPERAND,
    FUNCTIONAL_PARAMETER
  }

  enum Type implements Function<Object, RestrictionParameter<?>> {
    TYPE(
        Class.class,
        o ->
            () -> Optional.of(o)
                .filter(Class.class::isInstance)
                .map(obj -> (Class<?>) obj)
                .orElseThrow(
                    () ->
                        new IllegalArgumentException(
                            "parameter of type restriction must be Class<?>"))),
    SIZE(
        Integer.class,
        o ->
            () -> Optional.of(o)
                .filter(Integer.class::isInstance)
                .map(obj -> (Integer) obj)
                .orElseThrow(
                    () ->
                        new IllegalArgumentException(
                            "parameter of size restriction must be Integer"))),
    CONTEXT(
        ProcessingContext.class,
        o ->
            () -> Optional.of(o)
                .filter(ProcessingContext.class::isInstance)
                .map(obj -> (ProcessingContext) obj)
                .orElseThrow(
                    () ->
                        new IllegalArgumentException(
                            "parameter of context restriction must be ProcessingContext")));
    private final Class<?> parameterType;
    private final Function<Object, RestrictionParameter<?>> parameterConverter;

    Type(Class<?> parameterType, Function<Object, RestrictionParameter<?>> parameterConverter) {
      this.parameterType = parameterType;
      this.parameterConverter = parameterConverter;
    }

    public Class<?> parameterType() {
      return parameterType;
    }

    @Override
    public RestrictionParameter<?> apply(Object o) {
      return this.parameterConverter.apply(o);
    }
  }

  interface RestrictionParameter<P> extends Supplier<P> {

  }
}
