package io.github.anticipasean.ent.pattern;

import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Pattern {

    static Logger logger = LoggerFactory.getLogger(Pattern.class);

    static <S> IfMatchClause<S> forObject(S someObject) {
        return new IfMatchClauseImpl<>(someObject);
    }

    static <I, R> boolean isOfType(I inputObject,
                                   Class<R> returnType) {
        return inputObject != null && (Objects.requireNonNull(returnType,
                                                              () -> "returnType specified may not be null")
                                              .isAssignableFrom(inputObject.getClass()) || (inputObject.getClass()
                                                                                                       .isPrimitive()
            && returnType.isInstance(inputObject)));
    }

    static <I, R> Optional<R> tryDynamicCast(I inputObject,
                                             Class<R> returnType) {
        try {
            return Optional.of(inputObject)
                           .map(input -> Objects.requireNonNull(returnType,
                                                                () -> "returnType specified may not be null for dynamic casting")
                                                .cast(input));
        } catch (ClassCastException e) {
            return Optional.empty();
        }
    }

}



