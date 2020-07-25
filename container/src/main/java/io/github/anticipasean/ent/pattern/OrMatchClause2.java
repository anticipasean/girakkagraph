package io.github.anticipasean.ent.pattern;

import cyclops.control.Option;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface OrMatchClause2<K, V, O> {

    <I> OrMatchPredicate2<K, V, I, O> ifKeyFitsAndValueOfType(Predicate<K> condition, Class<I> possibleType);

    OrThenClause2<K, V, V, O> ifKeyValueFits(BiPredicate<K, V> condition);

    Option<O> get();

    O orElse(O defaultOutput);

    O orElseGet(Supplier<O> defaultOutputSupplier);

    <X extends RuntimeException> O orElseThrow(Supplier<X> throwableSupplier);

}
