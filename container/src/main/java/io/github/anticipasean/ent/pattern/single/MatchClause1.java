package io.github.anticipasean.ent.pattern.single;

import cyclops.companion.Streamable;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import io.github.anticipasean.ent.iterator.TypeMatchingIterable;
import io.github.anticipasean.ent.pattern.Clause;
import io.github.anticipasean.ent.pattern.VariantMapper;
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
                                          .flatMap(VariantMapper.inputTypeMapper(possibleType))
                                          .fold(i -> Tuple2.of(subject(),
                                                               Option.some(i)),
                                                () -> Tuple2.of(subject(),
                                                                Option.none())));
    }

    default <I> ThenClause1<V, I> isOfTypeAnd(Class<I> possibleType,
                                              Predicate<? super I> condition) {
        return ThenClause1.of(() -> Option.of(subject())
                                          .flatMap(VariantMapper.inputTypeMapper(possibleType))
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

    default <T, E> ThenIterableClause1<V, E> isIterableOver(Class<E> elementType) {
        return ThenIterableClause1.of(() -> Option.of(subject())
                                                  .flatMap(VariantMapper.inputTypeMapper(Iterable.class))
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
                                                  .flatMap(VariantMapper.inputTypeMapper(Iterable.class))
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

    @SuppressWarnings("unchecked")
    default <I> ThenOptionClause1<V, I> isOptionOfType(Class<I> inputType) {
        return ThenOptionClause1.of(() -> Option.of(subject())
                                                .flatMap(VariantMapper.inputTypeMapper(Option.class))
                                                .map(option -> option.orElse(null))
                                                .map(o -> Option.ofNullable(o)
                                                                .flatMap(VariantMapper.inputTypeMapper(inputType)))
                                                .fold(option -> Tuple2.of(subject(),
                                                                          Option.some(option)
                                                                                .filter(Option::isPresent)),
                                                      () -> Tuple2.of(subject(),
                                                                      Option.none())));
    }

    @SuppressWarnings("unchecked")
    default <I> ThenOptionClause1<V, I> isOptionOfTypeAnd(Class<I> inputType,
                                                          Predicate<I> condition) {
        return ThenOptionClause1.of(() -> Option.of(subject())
                                                .flatMap(VariantMapper.inputTypeMapper(Option.class))
                                                .map(option -> option.orElse(null))
                                                .map(o -> Option.ofNullable(o)
                                                                .flatMap(VariantMapper.inputTypeMapper(inputType))
                                                                .filter(condition))
                                                .fold(option -> Tuple2.of(subject(),
                                                                          Option.some(option)
                                                                                .filter(Option::isPresent)),
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
