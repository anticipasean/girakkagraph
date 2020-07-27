package io.github.anticipasean.ent.pattern;

import cyclops.companion.Streamable;
import java.util.function.Function;
import java.util.function.Predicate;

public interface MatchPredicate<V, I> {

    ThenClause<V, I> and(Predicate<I> condition);

    <O> OrMatchClause<V, I, O> then(Function<I, O> func);

}
