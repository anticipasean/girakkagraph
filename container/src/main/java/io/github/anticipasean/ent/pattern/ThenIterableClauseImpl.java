package io.github.anticipasean.ent.pattern;

import cyclops.companion.Streamable;
import java.util.function.Function;

public class ThenIterableClauseImpl<V, I> implements ThenIterableClause<V, I> {

    private final V valueObject;
    private final Iterable<I> inputIterable;

    public ThenIterableClauseImpl(V valueObject,
                                  Iterable<I> inputIterable) {
        this.valueObject = valueObject;
        this.inputIterable = inputIterable;
    }

    @Override
    public <O> OrMatchClause<V, I, O> then(Function<Streamable<I>, O> mapper) {
        if (inputIterable != null && inputIterable.iterator()
                                                  .hasNext()) {
            return new OrMatchClauseImpl<>(valueObject,
                                           mapper.apply(Streamable.fromIterable(inputIterable)));
        }
        return new OrMatchClauseImpl<>(valueObject,
                                       null);
    }
}
