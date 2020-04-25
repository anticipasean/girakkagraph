package io.github.anticipasean.ent.pattern;

import java.util.function.Function;
import java.util.function.Predicate;

public interface IfMatchPredicate<E, I> {

    ThenClause<E, I> and(Predicate<I> condition);

    <O> IfNotMatchClause<E, I, O> then(Function<I, O> func);

}
