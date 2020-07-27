package io.github.anticipasean.ent.pattern;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

public interface MatchClause2<K, V> {

    <I> MatchPredicate2<K, V, I> ifKeyFitsAndValueOfType(Predicate<K> condition, Class<I> possibleType);

    <I> MatchPredicate2<K, V, I> ifKeyValueFitsAndValueOfType(BiPredicate<K, V> condition, Class<I> possibleType);

    <I> MatchPredicate2<K, V, I> ifValueOfType(Class<I> possibleType);

    ThenClause2<K, V, V> ifKeyValueFits(BiPredicate<K, V> condition);

}
