package io.github.anticipasean.ent.pattern;

import java.util.function.BiFunction;
import java.util.function.Predicate;

public interface MatchPredicate2<K, V, I> {

    ThenClause2<K, V, I> andValueFits(Predicate<I> condition);

    <O> OrMatchClause2<K, V, O> then(BiFunction<K, I, O> keyValueMapper);

}
