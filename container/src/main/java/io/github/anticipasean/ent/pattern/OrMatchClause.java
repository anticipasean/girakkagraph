package io.github.anticipasean.ent.pattern;

import cyclops.control.Option;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface OrMatchClause<V, I, O> {

    <I> OrMatchPredicate<V, I, O> ifOfType(Class<I> possibleType);

    <E> OrMatchIterablePredicate<V, E, O> ifIterableOver(Class<E> elementType);

    OrThenClause<V, V, O> ifFits(Predicate<V> condition);

    Option<O> get();

    O orElse(O defaultOutput);

    O orElseGet(Supplier<O> defaultOutputSupplier);

    <X extends RuntimeException> O orElseThrow(Supplier<X> throwable);

}
