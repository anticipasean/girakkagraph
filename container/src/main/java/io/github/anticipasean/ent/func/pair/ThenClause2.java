package io.github.anticipasean.ent.func.pair;

import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import io.github.anticipasean.ent.func.Clause;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public interface ThenClause2<K, V, KI, VI> extends Clause<Tuple2<Tuple2<K, V>, Option<Tuple2<KI, VI>>>> {

    static <K, V, KI, VI> ThenClause2<K, V, KI, VI> of(Supplier<Tuple2<Tuple2<K, V>, Option<Tuple2<KI, VI>>>> valueSupplier) {
        return new ThenClause2<K, V, KI, VI>() {
            @Override
            public Tuple2<Tuple2<K, V>, Option<Tuple2<KI, VI>>> get() {
                return valueSupplier.get();
            }
        };
    }

    default <KO, VO> OrMatchClause2<K, V, KI, VI, KO, VO> then(BiFunction<KI, VI, Tuple2<KO, VO>> mapper) {
        return OrMatchClause2.of(() -> MatchResult2.of(subject().map2(kiviTupleOpt -> kiviTupleOpt.map(kiviTuple2 -> mapper.apply(kiviTuple2._1(),
                                                                                                                                  kiviTuple2._2())))
                                                                .fold((kvTuple2, kovoTuple2) -> kovoTuple2.toEither(Tuple2.of(kvTuple2,
                                                                                                                              Option.none())))));
    }

    default <KO, VO> OrMatchClause2<K, V, KI, VI, KO, VO> then(Function<KI, KO> keyMapper,
                                                               Function<VI, VO> valueMapper) {
        return OrMatchClause2.of(() -> MatchResult2.of(subject().map2(kiviTupleOpt -> kiviTupleOpt.map(kiviTuple2 -> kiviTuple2.map1(keyMapper)
                                                                                                                               .map2(valueMapper)))
                                                                .fold((kvTuple2, kovoTuple2) -> kovoTuple2.toEither(Tuple2.of(kvTuple2,
                                                                                                                              Option.none())))));
    }

    default <KO, VO> OrMatchClause2<K, V, KI, VI, KO, VO> then(Function<VI, VO> valueMapper) {
        return OrMatchClause2.of(() -> MatchResult2.of(subject()._2()
                                                                .map(Tuple2::_2)
                                                                .map(valueMapper)
                                                                .zip(Option.of(subject()._1()
                                                                                        ._1()))
                                                                .map(Tuple2::swap)
                                                                .toEither(Tuple2.of(subject()._1(),
                                                                                    Option.<Tuple2<KO, VO>>none()))));
    }

}
