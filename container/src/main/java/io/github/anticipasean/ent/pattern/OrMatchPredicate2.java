package io.github.anticipasean.ent.pattern;

import java.util.function.BiFunction;
import java.util.function.Predicate;

public interface OrMatchPredicate2<K, V, I, O> {

    OrThenClause2<K, V, I, O> and(Predicate<I> condition);

    OrMatchClause2<K, V, O> then(BiFunction<K, I, O> keyValueMapper);

}
