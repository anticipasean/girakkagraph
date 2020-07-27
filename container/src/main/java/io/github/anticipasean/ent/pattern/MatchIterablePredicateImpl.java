package io.github.anticipasean.ent.pattern;

import cyclops.companion.Streamable;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public class MatchIterablePredicateImpl<V, I> implements MatchIterablePredicate<V, I> {

    private final V valueObject;
    private final Iterable<I> inputIterable;

    public MatchIterablePredicateImpl(V valueObject,
                                      Iterable<I> inputIterable) {
        this.valueObject = valueObject;
        this.inputIterable = inputIterable;
    }

    @Override
    public ThenIterableClause<V, I> and(Predicate<Streamable<I>> condition) {

        if (inputIterable != null && inputIterable.iterator()
                                                  .hasNext() && Objects.nonNull(condition)
            && condition.test(Streamable.fromIterable(inputIterable))) {
            return new ThenIterableClauseImpl<>(valueObject,
                                                inputIterable);
        }
        return new ThenIterableClauseImpl<>(valueObject,
                                            null);

    }

    @Override
    public <O> OrMatchClause<V, I, O> then(Function<Streamable<I>, O> func) {
        if (inputIterable != null && inputIterable.iterator()
                                                  .hasNext()) {
            return new OrMatchClauseImpl<>(valueObject,
                                           func.apply(Streamable.fromIterable(inputIterable)));
        }
        return new OrMatchClauseImpl<>(valueObject,
                                       null);
    }
}
