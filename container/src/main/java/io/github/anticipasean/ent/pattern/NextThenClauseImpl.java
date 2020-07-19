package io.github.anticipasean.ent.pattern;

import java.util.Objects;
import java.util.function.Function;

public class NextThenClauseImpl<E, I, O> implements NextThenClause<E, I, O> {

    private final E eventObject;
    private final I matchedInput;
    private final O resultOutput;

    public NextThenClauseImpl(E eventObject,
                              I matchedInput,
                              O resultOutput) {
        this.eventObject = eventObject;
        this.matchedInput = matchedInput;
        this.resultOutput = resultOutput;
    }

    @Override
    public OrMatchClause<E, I, O> then(Function<I, O> func) {
        if (resultOutput != null) {
            return new OrMatchClauseImpl<E, I, O>(eventObject,
                                                  resultOutput);
        }
        if (matchedInput != null) {
            O resultOutput = Objects.requireNonNull(func,
                                                    () -> "func")
                                    .apply(matchedInput);
            return new OrMatchClauseImpl<E, I, O>(eventObject,
                                                  resultOutput);
        }
        return new OrMatchClauseImpl<>(eventObject, null);
    }
}
