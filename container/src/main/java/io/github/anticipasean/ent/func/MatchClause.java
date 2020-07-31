package io.github.anticipasean.ent.func;

import cyclops.companion.Streamable;
import cyclops.control.Option;
import java.util.function.Function;
import java.util.function.Predicate;

public interface MatchClause<V, I> {

    <I> ThenClause<V, V> isEqualTo(I otherObject);

    <I> ThenClause<V, I> isOfType(Class<I> possibleType);

    <I> ThenClause<V, I> isOfTypeAnd(Class<I> possibleType,
                                      Predicate<? super I> condition);

    ThenClause<V, V> fits(Predicate<? super V> condition);

    <T, E> ThenIterableClause<V, E> isIterableOver(Class<E> elementType);

    <T, E> ThenIterableClause<V, E> isIterableOverAnd(Class<E> elementType,
                                                       Predicate<Streamable<E>> condition);

    <I> ThenClause<V, I> mapsTo(Function<V, Option<I>> extractor);

    <I> ThenClause<V, I> mapsToAnd(Function<V, Option<I>> extractor,
                                    Predicate<I> condition);

}
