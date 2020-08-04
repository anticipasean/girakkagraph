package io.github.anticipasean.ent.func.pair;

import static io.github.anticipasean.ent.func.VariantMapper.inputTypeMapper;

import cyclops.companion.Streamable;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import io.github.anticipasean.ent.func.Clause;
import io.github.anticipasean.ent.iterator.TypeMatchingIterable;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface OrMatchClause2<K, V, KI, VI, KO, VO> extends Clause<MatchResult2<K, V, KI, VI, KO, VO>> {

    static <K, V, KI, VI, KO, VO> OrMatchClause2<K, V, KI, VI, KO, VO> of(Supplier<MatchResult2<K, V, KI, VI, KO, VO>> valueSupplier) {
        return new OrMatchClause2<K, V, KI, VI, KO, VO>() {
            @Override
            public MatchResult2<K, V, KI, VI, KO, VO> get() {
                return valueSupplier.get();
            }
        };
    }


    default <I> OrThenClause2<K, V, K, V, KO, VO> keyEqualTo(I otherObject) {
        return OrThenClause2.of(() -> MatchResult2.of(subject().either()
                                                               .mapLeft(kvkiviOptTuple -> kvkiviOptTuple._1()
                                                                                                        .first()
                                                                                                        .filter(k -> k.equals(otherObject)))
                                                               .mapLeft(kOpt -> Tuple2.of(subject().either()
                                                                                                   .leftOrElse(null)
                                                                                                   ._1(),
                                                                                          kOpt.map(k -> Tuple2.of(k,
                                                                                                                  subject().unapply()
                                                                                                                           ._1()
                                                                                                                           ._2()))))));

    }

    default <I> OrThenClause2<K, V, K, V, KO, VO> valueEqualTo(I otherObject) {
        return OrThenClause2.of(() -> MatchResult2.of(subject().either()
                                                               .mapLeft(kvkiviOptTuple -> kvkiviOptTuple._1()
                                                                                                        .second()
                                                                                                        .filter(v -> v.equals(otherObject)))
                                                               .mapLeft(vOpt -> Tuple2.of(subject().either()
                                                                                                   .leftOrElse(null)
                                                                                                   ._1(),
                                                                                          vOpt.map(v -> Tuple2.of(subject().unapply()
                                                                                                                           ._1()
                                                                                                                           ._1(),
                                                                                                                  v))))));

    }

    default <KI, VI> OrThenClause2<K, V, K, V, KO, VO> bothEqualTo(Tuple2<KI, VI> otherTuple) {
        return OrThenClause2.of(() -> MatchResult2.of(subject().either()
                                                               .mapLeft(kvkiviOptTuple -> kvkiviOptTuple.first()
                                                                                                        .filter(kvTuple -> kvTuple.equals(otherTuple)))
                                                               .mapLeft(kvTupleOpt -> Tuple2.of(subject().unapply()
                                                                                                         ._1(),
                                                                                                kvTupleOpt))));
    }

    default <KI> OrThenClause2<K, V, KI, V, KO, VO> keyOfType(Class<KI> possibleKeyType) {
        return OrThenClause2.of(() -> MatchResult2.of(subject().either()
                                                               .mapLeft(kvkiviTuple -> kvkiviTuple._1()
                                                                                                  .map1(inputTypeMapper(possibleKeyType)))
                                                               .mapLeft(kiOptVTuple -> Tuple2.of(subject().unapply()
                                                                                                          ._1(),
                                                                                                 kiOptVTuple._1()
                                                                                                            .map(ki -> Tuple2.of(ki,
                                                                                                                                 kiOptVTuple._2()))))));
    }

    default <VI> OrThenClause2<K, V, K, VI, KO, VO> valueOfType(Class<VI> possibleValueType) {
        return OrThenClause2.of(() -> MatchResult2.of(subject().either()
                                                               .mapLeft(kvkiviTuple -> kvkiviTuple._1()
                                                                                                  .map2(inputTypeMapper(possibleValueType)))
                                                               .mapLeft(kviOptTuple -> Tuple2.of(subject().unapply()
                                                                                                          ._1(),
                                                                                                 kviOptTuple._2()
                                                                                                            .map(vi -> Tuple2.of(kviOptTuple._1(),
                                                                                                                                 vi))))));

    }

    default <KI> OrThenClause2<K, V, KI, V, KO, VO> keyOfTypeAnd(Class<KI> possibleKeyType,
                                                                 Predicate<? super KI> condition) {
        return OrThenClause2.of(() -> MatchResult2.of(subject().either()
                                                               .mapLeft(kvkiviTuple -> kvkiviTuple._1()
                                                                                                  .map1(inputTypeMapper(possibleKeyType)))
                                                               .mapLeft(kiOptVTuple -> Tuple2.of(subject().unapply()
                                                                                                          ._1(),
                                                                                                 kiOptVTuple._1()
                                                                                                            .filter(condition)
                                                                                                            .map(ki -> Tuple2.of(ki,
                                                                                                                                 kiOptVTuple._2()))))));
    }

    default <VI> OrThenClause2<K, V, K, VI, KO, VO> valueOfTypeAnd(Class<VI> possibleValueType,
                                                                   Predicate<? super VI> condition) {
        return OrThenClause2.of(() -> MatchResult2.of(subject().either()
                                                               .mapLeft(kvkiviTuple -> kvkiviTuple._1()
                                                                                                  .map2(inputTypeMapper(possibleValueType)))
                                                               .mapLeft(kviOptTuple -> Tuple2.of(subject().unapply()
                                                                                                          ._1(),
                                                                                                 kviOptTuple._2()
                                                                                                            .filter(condition)
                                                                                                            .map(vi -> Tuple2.of(kviOptTuple._1(),
                                                                                                                                 vi))))));

    }

    default <KI, VI> OrThenClause2<K, V, KI, VI, KO, VO> bothOfType(Class<KI> possibleKeyType,
                                                                    Class<VI> possibleValueType) {
        return OrThenClause2.of(() -> MatchResult2.of(subject().either()
                                                               .mapLeft(kvkiviTuple -> kvkiviTuple._1()
                                                                                                  .bimap(inputTypeMapper(possibleKeyType),
                                                                                                         inputTypeMapper(possibleValueType))
                                                                                                  .fold(Option::zip))
                                                               .mapLeft(kiviTupleOpt -> Tuple2.of(subject().unapply()
                                                                                                           ._1(),
                                                                                                  kiviTupleOpt))));
    }

    default <KI, VI> OrThenClause2<K, V, KI, VI, KO, VO> bothOfTypeAnd(Class<KI> possibleKeyType,
                                                                       Class<VI> possibleValueType,
                                                                       BiPredicate<KI, VI> condition) {
        return OrThenClause2.of(() -> MatchResult2.of(subject().either()
                                                               .mapLeft(kvkiviTuple -> kvkiviTuple._1()
                                                                                                  .bimap(inputTypeMapper(possibleKeyType),
                                                                                                         inputTypeMapper(possibleValueType))
                                                                                                  .fold(Option::zip))
                                                               .mapLeft(kiviTupleOpt -> kiviTupleOpt.filter(kiviTuple2 -> condition.test(kiviTuple2._1(),
                                                                                                                                         kiviTuple2._2())))
                                                               .mapLeft(kiviTupleOpt -> Tuple2.of(subject().unapply()
                                                                                                           ._1(),
                                                                                                  kiviTupleOpt))));
    }

    default OrThenClause2<K, V, K, V, KO, VO> keyFits(Predicate<? super K> condition) {
        return OrThenClause2.of(() -> MatchResult2.of(subject().either()
                                                               .mapLeft(kvkiviTuple -> kvkiviTuple._1()
                                                                                                  .first()
                                                                                                  .filter(condition))
                                                               .mapLeft(kOpt -> Tuple2.of(subject().either()
                                                                                                   .leftOrElse(null)
                                                                                                   ._1(),
                                                                                          kOpt.map(k -> Tuple2.of(k,
                                                                                                                  subject().unapply()
                                                                                                                           ._1()
                                                                                                                           ._2()))))));
    }

    default OrThenClause2<K, V, K, V, KO, VO> valueFits(Predicate<? super V> condition) {
        return OrThenClause2.of(() -> MatchResult2.of(subject().either()
                                                               .mapLeft(kvkiviTuple -> kvkiviTuple._1()
                                                                                                  .second()
                                                                                                  .filter(condition))
                                                               .mapLeft(vOpt -> Tuple2.of(subject().either()
                                                                                                   .leftOrElse(null)
                                                                                                   ._1(),
                                                                                          vOpt.map(v -> Tuple2.of(subject().unapply()
                                                                                                                           ._1()
                                                                                                                           ._1(),
                                                                                                                  v))))));

    }

    default OrThenClause2<K, V, K, V, KO, VO> bothFit(BiPredicate<? super K, ? super V> condition) {
        return OrThenClause2.of(() -> MatchResult2.of(subject().either()
                                                               .mapLeft(kvkiviTuple -> Option.some(kvkiviTuple._1())
                                                                                             .filter(kvTuple -> condition.test(kvTuple._1(),
                                                                                                                               kvTuple._2())))
                                                               .mapLeft(kvTupleOpt -> Tuple2.of(subject().unapply()
                                                                                                         ._1(),
                                                                                                kvTupleOpt))));


    }

    default <VI> OrThenClause2<K, V, K, VI, KO, VO> keyFitsAndValueOfType(Predicate<? super K> condition,
                                                                          Class<VI> possibleValueType) {
        return OrThenClause2.of(() -> MatchResult2.of(subject().either()
                                                               .mapLeft(Tuple2::_1)
                                                               .mapLeft(kvTuple -> kvTuple.bimap(k -> Option.some(k)
                                                                                                            .filter(condition),
                                                                                                 inputTypeMapper(possibleValueType)))
                                                               .mapLeft(kOptViOptTuple -> Tuple2.of(subject().unapply()
                                                                                                             ._1(),
                                                                                                    kOptViOptTuple._1()
                                                                                                                  .zip(kOptViOptTuple._2())))));

    }

    default <VI> OrThenClause2<K, V, K, VI, KO, VO> valueOfTypeAndBothFit(Class<VI> possibleValueType,
                                                                          BiPredicate<? super K, ? super VI> condition) {
        return OrThenClause2.of(() -> MatchResult2.of(subject().either()
                                                               .mapLeft(Tuple2::_1)
                                                               .mapLeft(kvTuple -> kvTuple.map2(inputTypeMapper(possibleValueType))
                                                                                          .fold((k, viOpt) -> viOpt.filter(vi -> condition.test(k,
                                                                                                                                                vi))
                                                                                                                   .map(vi -> Tuple2.of(k,
                                                                                                                                        vi))))
                                                               .mapLeft(kviTupleOpt -> Tuple2.of(subject().unapply()
                                                                                                          ._1(),
                                                                                                 kviTupleOpt))));

    }

    default <T, E> OrThenIterableClause2<K, V, K, E, KO, VO> valueIterableOver(Class<E> possibleElementType) {
        return OrThenIterableClause2.of(() -> MatchResult2.of(subject().either()
                                                                       .mapLeft(Tuple2::_1)
                                                                       .mapLeft(Tuple2::_2)
                                                                       .mapLeft(inputTypeMapper(Iterable.class))
                                                                       .mapLeft(viOpt -> viOpt.map(iterable -> TypeMatchingIterable.of(iterable.iterator(),
                                                                                                                                       possibleElementType))
                                                                                              .filter(iterable -> iterable.iterator()
                                                                                                                          .hasNext()))
                                                                       .mapLeft(viIterOpt -> Tuple2.of(subject().unapply()
                                                                                                                ._1(),
                                                                                                       viIterOpt.map(viIterable -> Tuple2.of(subject().unapply()
                                                                                                                                                      ._1()
                                                                                                                                                      ._1(),
                                                                                                                                             viIterable))))));
    }

    default <T, E> OrThenIterableClause2<K, V, K, E, KO, VO> valueIterableOverAnd(Class<E> possibleElementType,
                                                                                  Predicate<Streamable<E>> condition) {
        return OrThenIterableClause2.of(() -> MatchResult2.of(subject().either()
                                                                       .mapLeft(Tuple2::_1)
                                                                       .mapLeft(Tuple2::_2)
                                                                       .mapLeft(inputTypeMapper(Iterable.class))
                                                                       .mapLeft(viOpt -> viOpt.map(iterable -> TypeMatchingIterable.of(iterable.iterator(),
                                                                                                                                       possibleElementType))
                                                                                              .filter(iterable -> iterable.iterator()
                                                                                                                          .hasNext())
                                                                                              .filter(iterable -> condition.test(Streamable.fromIterable(iterable))))
                                                                       .mapLeft(viIterOpt -> Tuple2.of(subject().either()
                                                                                                                .leftOrElse(null)
                                                                                                                ._1(),
                                                                                                       viIterOpt.map(viIterable -> Tuple2.of(subject().unapply()
                                                                                                                                                      ._1()
                                                                                                                                                      ._1(),
                                                                                                                                             viIterable))))));
    }

    default <KI> OrThenClause2<K, V, KI, V, KO, VO> keyMapsTo(Function<K, Option<KI>> keyMapper) {
        return OrThenClause2.of(() -> MatchResult2.of(subject().either()
                                                               .mapLeft(Tuple2::_1)
                                                               .mapLeft(Tuple2::_1)
                                                               .mapLeft(keyMapper)
                                                               .mapLeft(kiOpt -> Tuple2.of(subject().unapply()
                                                                                                    ._1(),
                                                                                           kiOpt.map(ki -> Tuple2.of(ki,
                                                                                                                     subject().unapply()
                                                                                                                              ._1()
                                                                                                                              ._2()))))));
    }

    default <KI> OrThenClause2<K, V, KI, V, KO, VO> keyMapsToAnd(Function<K, Option<KI>> keyMapper,
                                                                 Predicate<KI> condition) {
        return OrThenClause2.of(() -> MatchResult2.of(subject().either()
                                                               .mapLeft(Tuple2::_1)
                                                               .mapLeft(Tuple2::_1)
                                                               .mapLeft(keyMapper)
                                                               .mapLeft(kiOpt -> Tuple2.of(subject().unapply()
                                                                                                    ._1(),
                                                                                           kiOpt.filter(condition)
                                                                                                .map(ki -> Tuple2.of(ki,
                                                                                                                     subject().unapply()
                                                                                                                              ._1()
                                                                                                                              ._2()))))));
    }

    default <VI> OrThenClause2<K, V, K, VI, KO, VO> valueMapsTo(Function<V, Option<VI>> valueMapper) {
        return OrThenClause2.of(() -> MatchResult2.of(subject().either()
                                                               .mapLeft(Tuple2::_1)
                                                               .mapLeft(Tuple2::_2)
                                                               .mapLeft(valueMapper)
                                                               .mapLeft(viOpt -> Tuple2.of(subject().unapply()
                                                                                                    ._1(),
                                                                                           viOpt.map(vi -> Tuple2.of(subject().unapply()
                                                                                                                              ._1()
                                                                                                                              ._1(),
                                                                                                                     vi))))));
    }

    default <VI> OrThenClause2<K, V, K, VI, KO, VO> valueMapsToAnd(Function<V, Option<VI>> valueMapper,
                                                                   Predicate<VI> condition) {
        return OrThenClause2.of(() -> MatchResult2.of(subject().either()
                                                               .mapLeft(Tuple2::_1)
                                                               .mapLeft(Tuple2::_2)
                                                               .mapLeft(valueMapper)
                                                               .mapLeft(viOpt -> Tuple2.of(subject().unapply()
                                                                                                    ._1(),
                                                                                           viOpt.filter(condition)
                                                                                                .map(vi -> Tuple2.of(subject().unapply()
                                                                                                                              ._1()
                                                                                                                              ._1(),
                                                                                                                     vi))))));
    }

    default <KI, VI> OrThenClause2<K, V, KI, VI, KO, VO> bothMapTo(Function<K, Option<KI>> keyMapper,
                                                                   Function<V, Option<VI>> valueMapper) {
        return OrThenClause2.of(() -> MatchResult2.of(subject().either()
                                                               .mapLeft(Tuple2::_1)
                                                               .mapLeft(kvTuple -> kvTuple.bimap(k -> Option.of(k)
                                                                                                            .flatMap(keyMapper),
                                                                                                 v -> Option.of(v)
                                                                                                            .flatMap(valueMapper)))
                                                               .mapLeft(kiviTupleOfOpt -> kiviTupleOfOpt._1()
                                                                                                        .zip(kiviTupleOfOpt._2()))
                                                               .mapLeft(kiviTupleOpt -> Tuple2.of(subject().unapply()
                                                                                                           ._1(),
                                                                                                  kiviTupleOpt))));

    }

    default <KI, VI> OrThenClause2<K, V, KI, VI, KO, VO> bothMapToAnd(Function<K, Option<KI>> keyMapper,
                                                                      Function<V, Option<VI>> valueMapper,
                                                                      BiPredicate<KI, VI> condition) {
        return OrThenClause2.of(() -> MatchResult2.of(subject().either()
                                                               .mapLeft(Tuple2::_1)
                                                               .mapLeft(kvTuple -> kvTuple.bimap(keyMapper,
                                                                                                 valueMapper))
                                                               .mapLeft(kiviTupleOfOpt -> kiviTupleOfOpt._1()
                                                                                                        .zip(kiviTupleOfOpt._2()))
                                                               .mapLeft(kiviTupleOpt -> kiviTupleOpt.filter(kiviTuple2 -> condition.test(kiviTuple2._1(),
                                                                                                                                         kiviTuple2._2())))
                                                               .mapLeft(kiviTupleOpt -> Tuple2.of(subject().unapply()
                                                                                                           ._1(),
                                                                                                  kiviTupleOpt))));

    }

    default Option<Tuple2<KO, VO>> elseYield() {
        return subject().either()
                        .toOption();
    }

    default Option<KO> elseYieldKey() {
        return subject().either()
                        .toOption().map(Tuple2::_1);
    }

    default Option<VO> elseYieldValue() {
        return subject().either()
                        .toOption().map(Tuple2::_2);
    }

    default Tuple2<KO, VO> elseDefault(Tuple2<KO, VO> defaultOutput) {
        return subject().either()
                        .orElse(defaultOutput);
    }

    default KO elseDefaultKey(KO defaultKey) {
        return subject().either()
                        .orElse(Tuple2.of(defaultKey,
                                          null))
                        ._1();
    }

    default VO elseDefaultValue(VO defaultValue) {
        return subject().either()
                        .orElse(Tuple2.of(null,
                                          defaultValue))
                        ._2();
    }

    default Tuple2<KO, VO> elseGet(Supplier<Tuple2<KO, VO>> tupleSupplier) {
        return subject().either()
                        .orElseGet(tupleSupplier);
    }

    default <X extends RuntimeException> Tuple2<KO, VO> elseThrow(Supplier<X> throwableSupplier) {
        if (subject().either()
                     .isRight()) {
            return subject().either()
                            .orElse(null);
        }
        throw throwableSupplier.get();
    }
}
