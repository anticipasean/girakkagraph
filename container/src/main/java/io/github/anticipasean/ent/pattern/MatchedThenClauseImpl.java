package io.github.anticipasean.ent.pattern;

import java.util.Objects;
import java.util.function.Function;

public class MatchedThenClauseImpl<E, I, O> implements MatchedThenClause<E, I, O> {

    private final E eventObject;
    private final I matchedInput;
    private final O resultOutput;

    public MatchedThenClauseImpl(E eventObject,
                                 I matchedInput,
                                 O resultOutput) {
        this.eventObject = eventObject;
        this.matchedInput = matchedInput;
        this.resultOutput = resultOutput;
    }

    @Override
    public IfNotMatchClause<E, I, O> then(Function<I, O> func) {
        if (resultOutput != null) {
            return new IfNotMatchClauseImpl<E, I, O>(eventObject,
                                                     resultOutput);
        }
        if (matchedInput != null) {
            O resultOutput = Objects.requireNonNull(func,
                                                    () -> "functionForInput may not be null")
                                    .apply(matchedInput);
            return new IfNotMatchClauseImpl<E, I, O>(eventObject,
                                                     resultOutput);
        }
        throw new IllegalStateException("MatchedThenClause should not be called without resultOutput or matchedInput");
    }
}
