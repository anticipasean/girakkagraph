package io.github.anticipasean.ent.pattern.pair;

import cyclops.companion.Streamable;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import io.github.anticipasean.ent.iterator.TypeMatchingIterable;
import io.github.anticipasean.ent.pattern.Clause;
import io.github.anticipasean.ent.pattern.VariantMapper;
import java.util.Iterator;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface MatchClause2<K, V> extends Clause<Tuple2<K, V>> {

    static <K, V> MatchClause2<K, V> of(Supplier<Tuple2<K, V>> valueSupplier) {
        return new MatchClause2<K, V>() {
            @Override
            public Tuple2<K, V> get() {
                return valueSupplier.get();
            }
        };
    }

    default <I> ThenClause2<K, V, K, V> keyEqualTo(I otherObject) {
        return ThenClause2.of(() -> Tuple2.of(subject(),
                                              Option.of(subject()._1())
                                                    .filter(k -> k.equals(otherObject))
                                                    .map(k -> subject())));
    }

    default <I> ThenClause2<K, V, K, V> valueEqualTo(I otherObject) {
        return ThenClause2.of(() -> Tuple2.of(subject(),
                                              Option.of(subject()._2())
                                                    .filter(v -> v.equals(otherObject))
                                                    .map(v -> subject())));

    }

    default <KI, VI> ThenClause2<K, V, K, V> bothEqualTo(Tuple2<KI, VI> otherTuple) {
        return ThenClause2.of(() -> Tuple2.of(subject(),
                                              Option.of(subject())
                                                    .filter(tuple2 -> tuple2.equals(otherTuple))));

    }

    default <KI> ThenClause2<K, V, KI, V> keyOfType(Class<KI> possibleKeyType) {
        return ThenClause2.of(() -> subject().map1(VariantMapper.inputTypeMapper(possibleKeyType))
                                             .fold((kiOpt, v) -> Tuple2.of(subject(),
                                                                           kiOpt.map(ki -> Tuple2.of(ki,
                                                                                                     v)))));
    }

    default <VI> ThenClause2<K, V, K, VI> valueOfType(Class<VI> possibleValueType) {
        return ThenClause2.of(() -> subject().map2(VariantMapper.inputTypeMapper(possibleValueType))
                                             .fold((k, viOption) -> Tuple2.of(subject(),
                                                                              viOption.map(vi -> Tuple2.of(k,
                                                                                                           vi)))));

    }

    default <KI> ThenClause2<K, V, KI, V> keyOfTypeAnd(Class<KI> possibleKeyType,
                                                       Predicate<? super KI> condition) {
        return ThenClause2.of(() -> subject().map1(VariantMapper.inputTypeMapper(possibleKeyType))
                                             .fold((kiOpt, v) -> Tuple2.of(subject(),
                                                                           kiOpt.filter(condition)
                                                                                .map(ki -> Tuple2.of(ki,
                                                                                                     v)))));

    }

    default <VI> ThenClause2<K, V, K, VI> valueOfTypeAnd(Class<VI> possibleValueType,
                                                         Predicate<? super VI> condition) {
        return ThenClause2.of(() -> subject().map2(VariantMapper.inputTypeMapper(possibleValueType))
                                             .fold((k, viOption) -> Tuple2.of(subject(),
                                                                              viOption.filter(condition)
                                                                                      .map(vi -> Tuple2.of(k,
                                                                                                           vi)))));

    }

    default <VI> ThenClause2<K, V, K, VI> keyFitsAndValueOfType(Predicate<? super K> condition,
                                                                Class<VI> possibleValueType) {
        return ThenClause2.of(() -> subject().bimap(k -> Option.some(k)
                                                               .filter(condition),
                                                    VariantMapper.inputTypeMapper(possibleValueType))
                                             .fold((kOption, viOption) -> Tuple2.of(subject(),
                                                                                    kOption.zip(viOption))));

    }

    default <VI> ThenClause2<K, V, K, VI> valueOfTypeAndBothFit(Class<VI> possibleValueType,
                                                                BiPredicate<K, VI> condition) {
        return ThenClause2.of(() -> subject().map2(VariantMapper.inputTypeMapper(possibleValueType))
                                             .fold((k, viOpt) -> viOpt.filter(vi -> condition.test(k,
                                                                                                   vi))
                                                                      .map(vi -> Tuple2.of(k,
                                                                                           vi)))
                                             .to(kviTupleOpt -> Tuple2.of(subject(),
                                                                          kviTupleOpt)));

    }

    default <KI, VI> ThenClause2<K, V, KI, VI> bothOfType(Class<KI> possibleKeyType,
                                                          Class<VI> possibleValueType) {
        return ThenClause2.of(() -> subject().bimap(VariantMapper.inputTypeMapper(possibleKeyType),
                                                    VariantMapper.inputTypeMapper(possibleValueType))
                                             .fold((kiOpt, viOpt) -> Tuple2.of(subject(),
                                                                               kiOpt.zip(viOpt))));


    }

    default <KI, VI> ThenClause2<K, V, KI, VI> bothOfTypeAnd(Class<KI> possibleKeyType,
                                                             Class<VI> possibleValueType,
                                                             BiPredicate<KI, VI> condition) {
        return ThenClause2.of(() -> subject().bimap(VariantMapper.inputTypeMapper(possibleKeyType),
                                                    VariantMapper.inputTypeMapper(possibleValueType))
                                             .fold((kiOpt, viOpt) -> Tuple2.of(subject(),
                                                                               kiOpt.zip(viOpt)
                                                                                    .filter(kiviTuple2 -> condition.test(kiviTuple2._1(),
                                                                                                                         kiviTuple2._2())))));

    }

    default ThenClause2<K, V, K, V> keyFits(Predicate<? super K> condition) {
        return ThenClause2.of(() -> Tuple2.of(subject(),
                                              Option.of(subject()._1())
                                                    .filter(condition)
                                                    .map(k -> subject())));
    }

    default ThenClause2<K, V, K, V> valueFits(Predicate<? super V> condition) {
        return ThenClause2.of(() -> Tuple2.of(subject(),
                                              Option.of(subject()._2())
                                                    .filter(condition)
                                                    .map(v -> subject())));

    }

    default ThenClause2<K, V, K, V> bothFit(BiPredicate<? super K, ? super V> condition) {
        return ThenClause2.of(() -> Tuple2.of(subject(),
                                              Option.of(subject())
                                                    .filter(kvTuple -> condition.test(kvTuple._1(),
                                                                                      kvTuple._2()))));

    }

    default <T, E> ThenIterableClause2<K, V, K, E> valueIterableOver(Class<E> possibleElementType) {
        return ThenIterableClause2.of(() -> subject().map2(VariantMapper.inputTypeMapper(Iterable.class))
                                                     .map2(iterableOpt -> iterableOpt.map(iterable -> TypeMatchingIterable.of(iterable.iterator(),
                                                                                                                              possibleElementType))
                                                                                     .filter(iterable -> iterable.iterator()
                                                                                                                 .hasNext()))
                                                     .fold((k, iterableOpt) -> Tuple2.of(subject(),
                                                                                         iterableOpt.map(iter -> Tuple2.of(k,
                                                                                                                           iter)))));
    }

    default <T, E> ThenIterableClause2<K, V, K, E> valueIterableOverAnd(Class<E> possibleElementType,
                                                                        Predicate<Streamable<E>> condition) {
        return ThenIterableClause2.of(() -> subject().map2(VariantMapper.inputTypeMapper(Iterable.class))
                                                     .map2(iterableOpt -> iterableOpt.map(iterable -> TypeMatchingIterable.of(iterable.iterator(),
                                                                                                                              possibleElementType))
                                                                                     .filter(iter -> Option.some(iter.iterator())
                                                                                                           .filter(Iterator::hasNext)
                                                                                                           .map(Streamable::fromIterator)
                                                                                                           .filter(condition)
                                                                                                           .isPresent()))
                                                     .fold((k, iterableOpt) -> Tuple2.of(subject(),
                                                                                         iterableOpt.map(iter -> Tuple2.of(k,
                                                                                                                           iter)))));


    }

    @SuppressWarnings("unchecked")
    default <VI> ThenOptionClause2<K, V, K, VI> valueOptionOfType(Class<VI> inputType) {
        return ThenOptionClause2.of(() -> subject().map2(VariantMapper.inputTypeMapper(Option.class))
                                                   .map2(optOpt -> optOpt.orElse(Option.none()))
                                                   .map2(option -> option.orElse(null))
                                                   .map2(o -> Option.ofNullable(o)
                                                                    .flatMap(VariantMapper.inputTypeMapper(inputType)))
                                                   .fold((k, opt) -> Tuple2.of(subject(),
                                                                               Option.of(opt)
                                                                                     .filter(Option::isPresent)
                                                                                     .map(viOption -> Tuple2.of(k,
                                                                                                                viOption)))));
    }

    @SuppressWarnings("unchecked")
    default <VI> ThenOptionClause2<K, V, K, VI> valueOptionOfTypeAnd(Class<VI> inputType,
                                                                     Predicate<Option<VI>> condition) {
        return ThenOptionClause2.of(() -> subject().map2(VariantMapper.inputTypeMapper(Option.class))
                                                   .map2(optOpt -> optOpt.orElse(Option.none()))
                                                   .map2(option -> option.orElse(null))
                                                   .map2(o -> Option.ofNullable(o)
                                                                    .flatMap(VariantMapper.inputTypeMapper(inputType)))
                                                   .fold((k, viOpt) -> Tuple2.of(subject(),
                                                                               Option.of(viOpt)
                                                                                     .filter(Option::isPresent)
                                                                                     .filter(condition)
                                                                                     .map(viOption -> Tuple2.of(k,
                                                                                                                viOption)))));
    }

    default <KI> ThenClause2<K, V, KI, V> keyMapsTo(Function<K, Option<KI>> keyMapper) {
        return ThenClause2.of(() -> subject().map1(keyMapper)
                                             .fold((kiOpt, v) -> Tuple2.of(subject(),
                                                                           kiOpt.map(ki -> Tuple2.of(ki,
                                                                                                     v)))));

    }

    default <KI> ThenClause2<K, V, KI, V> keyMapsToAnd(Function<K, Option<KI>> keyMapper,
                                                       Predicate<KI> condition) {
        return ThenClause2.of(() -> subject().map1(keyMapper)
                                             .fold((kiOpt, v) -> Tuple2.of(subject(),
                                                                           kiOpt.filter(condition)
                                                                                .map(ki -> Tuple2.of(ki,
                                                                                                     v)))));
    }

    default <VI> ThenClause2<K, V, K, VI> valueMapsTo(Function<V, Option<VI>> valueMapper) {
        return ThenClause2.of(() -> subject().map2(valueMapper)
                                             .fold((k, viOption) -> Tuple2.of(subject(),
                                                                              viOption.map(vi -> Tuple2.of(k,
                                                                                                           vi)))));

    }

    default <VI> ThenClause2<K, V, K, VI> valueMapsToAnd(Function<V, Option<VI>> valueMapper,
                                                         Predicate<VI> condition) {
        return ThenClause2.of(() -> subject().map2(valueMapper)
                                             .fold((k, viOption) -> Tuple2.of(subject(),
                                                                              viOption.filter(condition)
                                                                                      .map(vi -> Tuple2.of(k,
                                                                                                           vi)))));
    }

    default <KI, VI> ThenClause2<K, V, KI, VI> bothMapTo(Function<K, Option<KI>> keyMapper,
                                                         Function<V, Option<VI>> valueMapper) {
        return ThenClause2.of(() -> subject().bimap(keyMapper,
                                                    valueMapper)
                                             .fold((kiOpt, viOpt) -> Tuple2.of(subject(),
                                                                               kiOpt.zip(viOpt))));

    }

    default <KI, VI> ThenClause2<K, V, KI, VI> bothMapToAnd(Function<K, Option<KI>> keyMapper,
                                                            Function<V, Option<VI>> valueMapper,
                                                            BiPredicate<KI, VI> condition) {
        return ThenClause2.of(() -> subject().bimap(keyMapper,
                                                    valueMapper)
                                             .fold((kiOpt, viOpt) -> kiOpt.zip(viOpt)
                                                                          .filter(kiviTuple2 -> condition.test(kiviTuple2._1(),
                                                                                                               kiviTuple2._2()))
                                                                          .fold(tuple -> Tuple2.of(subject(),
                                                                                                   Option.some(tuple)),
                                                                                () -> Tuple2.of(subject(),
                                                                                                Option.none()))));


    }

}
