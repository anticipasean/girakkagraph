package io.github.anticipasean.ent.pattern;

import java.util.function.Predicate;

public interface MatchClause<V> {

    <I> MatchPredicate<V, I> ifOfType(Class<I> possibleType);

    <E> MatchIterablePredicate<V, E> ifIterableOver(Class<E> elementType);

    ThenClause<V, V> ifFits(Predicate<V> condition);

}
