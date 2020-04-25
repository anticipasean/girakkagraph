package io.github.anticipasean.ent.pattern;

import java.util.function.Function;

public class UnmatchedThenClauseImpl<E, I, O> implements UnmatchedThenClause<E, I, O> {

    private final E eventObject;

    public UnmatchedThenClauseImpl(E eventObject) {
        this.eventObject = eventObject;
    }

    @Override
    public IfNotMatchClause<E, I, O> then(Function<I, O> func) {
        return new IfNotMatchClauseImpl<>(eventObject,
                                          null);
    }
}
