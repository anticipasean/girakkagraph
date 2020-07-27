package io.github.anticipasean.ent.pattern;

import cyclops.companion.Streamable;
import java.util.function.Function;
import java.util.function.Predicate;

public interface MatchIterablePredicate<V, I> {

    ThenIterableClause<V, I> and(Predicate<Streamable<I>> condition);

    <O> OrMatchClause<V, I, O> then(Function<Streamable<I>, O> func);

}
