package io.github.anticipasean.ent.pattern;

import cyclops.control.Option;
import cyclops.control.Try;
import java.util.function.Function;
import java.util.function.Predicate;

public interface VariantMapper {

    static <I, R> boolean isOfType(I inputObject,
                                   Class<R> returnType) {
        return inputObject != null && returnType != null && (returnType.isAssignableFrom(inputObject.getClass()) || (
            inputObject.getClass()
                       .isPrimitive() && returnType.isInstance(inputObject)));
    }

    static <I, R> Option<R> tryDynamicCastOfInputToReturnType(I inputObject,
                                                              Class<R> returnType) {
        if (inputObject == null || returnType == null) {
            return Option.none();
        }

        return Try.withCatch(() -> returnType.cast(inputObject),
                             ClassCastException.class)
                  .toOption();
    }

    static <I, R> Predicate<? super I> inputTypeFilter(Class<R> returnType) {
        return i -> isOfType(i,
                             returnType);
    }

    static <I, R> Function<I, Option<R>> inputTypeMapper(Class<R> returnType) {
        return i -> Option.ofNullable(i)
                          .filter(inputTypeFilter(returnType))
                          .flatMap(dynamicCaster(returnType));
    }

    static <I, R> Function<I, Option<R>> dynamicCaster(Class<R> returnType) {
        return i -> tryDynamicCastOfInputToReturnType(i,
                                                      returnType);
    }

}
