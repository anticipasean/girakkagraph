package io.github.anticipasean.ent.pattern;

import java.util.Objects;
import java.util.function.Function;

public class ThenClauseImpl<E, I> implements ThenClause<E, I> {

    private final E eventObject;
    private final I matchedInput;

    public ThenClauseImpl(E eventObject,
                          I matchedInput) {
        this.eventObject = eventObject;
        this.matchedInput = matchedInput;
    }

    @Override
    public <O> OrMatchClause<E, I, O> then(Function<I, O> func) {
        if (matchedInput != null) {
            O resultOutput = Objects.requireNonNull(func,
                                                    () -> "functionForInput may not be null")
                                    .apply(matchedInput);
            return new OrMatchClauseImpl<E, I, O>(eventObject,
                                                  resultOutput);
        }
        return new OrMatchClauseImpl<>(eventObject,
                                       null);
    }
}
