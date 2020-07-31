package io.github.anticipasean.ent.func;

import cyclops.companion.Streamable;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import io.github.anticipasean.ent.iterator.TypeCheckingIterator;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface MatchClause1<V> extends Clause1<V> {

    static <V> MatchClause1<V> of(Supplier<V> valueSupplier) {
        return new MatchClause1<V>() {
            @Override
            public V get() {
                return valueSupplier.get();
            }
        };
    }

    default <I> ThenClause1<V, V> isEqualTo(I otherObject) {
        return ThenClause1.of(() -> Option.of(subject())
                                          .filter(v -> v.equals(otherObject))
                                          .fold(v -> Tuple2.of(subject(),
                                                               Option.some(v)),
                                                () -> Tuple2.of(subject(),
                                                                Option.none())));
    }

    default <I> ThenClause1<V, I> isOfType(Class<I> possibleType) {
        return ThenClause1.of(() -> Option.of(subject())
                                          .flatMap(inputTypeMapper(possibleType))
                                          .fold(i -> Tuple2.of(subject(),
                                                               Option.some(i)),
                                                () -> Tuple2.of(subject(),
                                                                Option.none())));
    }

    default <I> ThenClause1<V, I> isOfTypeAndFits(Class<I> possibleType,
                                                  Predicate<? super I> condition) {
        return ThenClause1.of(() -> Option.of(subject())
                                          .flatMap(inputTypeMapper(possibleType))
                                          .filter(condition)
                                          .fold(i -> Tuple2.of(subject(),
                                                               Option.some(i)),
                                                () -> Tuple2.of(subject(),
                                                                Option.none())));
    }

    default ThenClause1<V, V> fits(Predicate<? super V> condition) {
        return ThenClause1.of(() -> Option.of(subject())
                                          .filter(condition)
                                          .fold(i -> Tuple2.of(subject(),
                                                               Option.some(i)),
                                                () -> Tuple2.of(subject(),
                                                                Option.none())));
    }

    //TODO: Test reusing iterator
    default <T, E> ThenIterableClause1<V, E> isIterableOver(Class<E> elementType) {
        return ThenIterableClause1.of(() -> Option.of(subject())
                                                  .flatMap(inputTypeMapper(Iterable.class))
                                                  .map(iterable -> new TypeCheckingIterator<T, E>(iterable.iterator(),
                                                                                                  elementType))
                                                  .filter(TypeCheckingIterator::hasNext)
                                                  .map(iter -> (Iterable<E>) (() -> iter))
                                                  .fold(iterable -> Tuple2.of(subject(),
                                                                              Option.some(iterable)),
                                                        () -> Tuple2.of(subject(),
                                                                        Option.none())));
    }

    default <T, E> ThenIterableClause1<V, E> isIterableOverAnd(Class<E> elementType,
                                                               Predicate<Streamable<E>> condition) {
        return ThenIterableClause1.of(() -> Option.of(subject())
                                                  .flatMap(inputTypeMapper(Iterable.class))
                                                  .map(iterable -> new TypeCheckingIterator<T, E>(iterable.iterator(),
                                                                                                  elementType))
                                                  .filter(TypeCheckingIterator::hasNext)
                                                  .map(iter -> (Iterable<E>) (() -> iter))
                                                  .map(Streamable::fromIterable)
                                                  .filter(condition)
                                                  .fold(streamable -> Tuple2.of(subject(),
                                                                                Option.some((Iterable<E>) streamable::iterator)),
                                                        () -> Tuple2.of(subject(),
                                                                        Option.none())));
    }

}
