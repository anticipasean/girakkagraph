package io.github.anticipasean.ent.pattern;

import cyclops.companion.Streamable;
import java.util.function.Function;
import java.util.function.Predicate;

public interface OrMatchIterablePredicate<V, I, O> {

    OrThenIterableClause<V, I, O> and(Predicate<Streamable<I>> condition);

    OrMatchClause<V, I, O> then(Function<Streamable<I>, O> func);

}
