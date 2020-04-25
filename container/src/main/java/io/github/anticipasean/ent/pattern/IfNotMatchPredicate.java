package io.github.anticipasean.ent.pattern;

import java.util.function.Function;
import java.util.function.Predicate;

public interface IfNotMatchPredicate<E, I, O> {

    NextThenClause<E, I, O> and(Predicate<I> condition);

    IfNotMatchClause<E, I, O> then(Function<I, O> func);
}
