package io.github.anticipasean.ent.pattern;

import static java.util.Objects.nonNull;

import cyclops.companion.Streamable;
import java.util.function.Function;
import java.util.function.Predicate;

public class OrMatchIterablePredicateImpl<V, I, O> implements OrMatchIterablePredicate<V, I, O> {

    private final V valueObject;
    private final Iterable<I> inputIterable;
    private final O resultObject;

    public OrMatchIterablePredicateImpl(V valueObject,
                                        Iterable<I> inputIterable,
                                        O resultObject) {
        this.valueObject = valueObject;
        this.inputIterable = inputIterable;
        this.resultObject = resultObject;
    }

    @Override
    public OrThenIterableClause<V, I, O> and(Predicate<Streamable<I>> condition) {
        if (resultObject != null) {
            return new OrThenIterableClauseImpl<>(valueObject,
                                                  null,
                                                  resultObject);
        } else if (inputIterable != null && inputIterable.iterator()
                                                         .hasNext() && nonNull(condition)
            && condition.test(Streamable.fromIterable(inputIterable))) {
            return new OrThenIterableClauseImpl<>(valueObject,
                                                  inputIterable,
                                                  null);
        } else {
            return new OrThenIterableClauseImpl<>(valueObject,
                                                  null,
                                                  null);
        }
    }

    @Override
    public OrMatchClause<V, I, O> then(Function<Streamable<I>, O> func) {
        if (resultObject != null) {
            return new OrMatchClauseImpl<>(valueObject,
                                           resultObject);
        } else if (inputIterable != null && inputIterable.iterator()
                                                         .hasNext() && nonNull(func)) {
            return new OrMatchClauseImpl<>(valueObject,
                                           func.apply(Streamable.fromIterable(inputIterable)));
        } else {
            return new OrMatchClauseImpl<>(valueObject,
                                           null);
        }
    }
}
