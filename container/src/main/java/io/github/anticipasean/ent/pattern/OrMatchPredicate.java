package io.github.anticipasean.ent.pattern;

import java.util.function.Function;
import java.util.function.Predicate;

public interface OrMatchPredicate<E, I, O> {

    OrThenClause<E, I, O> and(Predicate<I> condition);

    OrMatchClause<E, I, O> then(Function<I, O> func);
}
