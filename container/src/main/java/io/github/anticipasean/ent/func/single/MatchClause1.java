package io.github.anticipasean.ent.func.single;

import static io.github.anticipasean.ent.func.VariantMapper.inputTypeMapper;

import cyclops.companion.Streamable;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import io.github.anticipasean.ent.func.Clause;
import io.github.anticipasean.ent.iterator.TypeMatchingIterable;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface MatchClause1<V> extends Clause<V> {

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

    default <I> ThenClause1<V, I> isOfTypeAnd(Class<I> possibleType,
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
                                                  .map(iterable -> TypeMatchingIterable.of(iterable.iterator(),
                                                                                           elementType))
                                                  .filter(iterable -> iterable.iterator()
                                                                              .hasNext())
                                                  .fold(iterable -> Tuple2.of(subject(),
                                                                              Option.some(iterable)),
                                                        () -> Tuple2.of(subject(),
                                                                        Option.none())));
    }

    default <T, E> ThenIterableClause1<V, E> isIterableOverAnd(Class<E> elementType,
                                                               Predicate<Streamable<E>> condition) {
        return ThenIterableClause1.of(() -> Option.of(subject())
                                                  .flatMap(inputTypeMapper(Iterable.class))
                                                  .map(iterable -> TypeMatchingIterable.of(iterable.iterator(),
                                                                                           elementType))
                                                  .filter(iterable -> iterable.iterator()
                                                                              .hasNext())
                                                  .filter(iterable -> condition.test(Streamable.fromIterable(iterable)))
                                                  .fold(iterable -> Tuple2.of(subject(),
                                                                              Option.some(iterable)),
                                                        () -> Tuple2.of(subject(),
                                                                        Option.none())));
    }

    default <I> ThenClause1<V, I> mapsTo(Function<V, Option<I>> extractor) {
        return ThenClause1.of(() -> Option.of(subject())
                                          .map(extractor)
                                          .fold(iOpt -> Tuple2.of(subject(),
                                                                  iOpt),
                                                () -> Tuple2.of(subject(),
                                                                Option.none())));
    }

    default <I> ThenClause1<V, I> mapsToAnd(Function<V, Option<I>> extractor,
                                            Predicate<I> condition) {
        return ThenClause1.of(() -> Option.of(subject())
                                          .map(extractor)
                                          .fold(iOpt -> Tuple2.of(subject(),
                                                                  iOpt.filter(condition)),
                                                () -> Tuple2.of(subject(),
                                                                Option.none())));
    }

}
