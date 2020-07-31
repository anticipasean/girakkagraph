package io.github.anticipasean.ent.func;

import cyclops.control.Option;
import cyclops.data.tuple.Tuple1;
import java.util.function.Function;
import java.util.function.Predicate;
import org.slf4j.LoggerFactory;

public interface Divergent {

    default <I, R> boolean isOfType(I inputObject,
                                   Class<R> returnType) {
        return inputObject != null && returnType != null && (returnType.isAssignableFrom(inputObject.getClass()) || (
            inputObject.getClass()
                       .isPrimitive() && returnType.isInstance(inputObject)));
    }

    default <I, R> Option<R> tryDynamicCastOfInputToReturnType(I inputObject,
                                                              Class<R> returnType) {
        if (inputObject == null || returnType == null) {
            return Option.none();
        }
        try {
            return Tuple1.of(inputObject)
                         .map(returnType::cast)
                         .fold(Option::some);
        } catch (ClassCastException e) {
            LoggerFactory.getLogger(Divergent.class).error("class_cast_exception: ", e);
            return Option.none();
        }
    }

    default <I, R> Predicate<? super I> inputTypeFilter(Class<R> returnType) {
        return i -> isOfType(i,
                             returnType);
    }

    default <I, R> Function<I, Option<R>> inputTypeMapper(Class<R> returnType) {
        return i -> Option.ofNullable(i)
                          .filter(inputTypeFilter(returnType))
                          .map(dynamicCaster(returnType))
                          .orElse(Option.none());
    }

    default <I, R> Function<I, Option<R>> dynamicCaster(Class<R> returnType) {
        return i -> tryDynamicCastOfInputToReturnType(i,
                                                      returnType);
    }

}
