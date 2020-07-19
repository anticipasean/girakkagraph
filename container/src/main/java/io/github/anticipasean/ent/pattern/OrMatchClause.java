package io.github.anticipasean.ent.pattern;

import cyclops.control.Option;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface OrMatchClause<E, I, O> {

    <I> OrMatchPredicate<E, I, O> ifOfType(Class<I> possibleType);

    NextThenClause<E, E, O> ifFits(Predicate<E> condition);

    Option<O> get();

    O orElse(O defaultOutput);

    O orElseGet(Supplier<O> defaultOutputSupplier);

    <X extends RuntimeException> O orElseThrow(Supplier<X> throwable);

}
