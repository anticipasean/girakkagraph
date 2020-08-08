package io.github.anticipasean.ent.pattern.pair;

import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import io.github.anticipasean.ent.pattern.Clause;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public interface ThenOptionClause2<K, V, KI, VI> extends Clause<Tuple2<Tuple2<K, V>, Option<Tuple2<KI, Option<VI>>>>> {

    static <K, V, KI, VI> ThenOptionClause2<K, V, KI, VI> of(Supplier<Tuple2<Tuple2<K, V>, Option<Tuple2<KI, Option<VI>>>>> tuple2Supplier) {
        return new ThenOptionClause2<K, V, KI, VI>() {
            @Override
            public Tuple2<Tuple2<K, V>, Option<Tuple2<KI, Option<VI>>>> get() {
                return tuple2Supplier.get();
            }
        };
    }

    default <KO, VO> OrMatchClause2<K, V, KI, VI, KO, VO> then(Function<Tuple2<KI, Option<VI>>, Tuple2<KO, VO>> mapper) {
        return OrMatchClause2.of(() -> MatchResult2.of(subject().map2(kiViOptTuple -> kiViOptTuple.map(mapper))
                                                                .fold((kvTuple, kiViOptTuple) -> kiViOptTuple.toEither(Tuple2.of(kvTuple,
                                                                                                                                 Option.none())))));
    }

    default <KO, VO> OrMatchClause2<K, V, KI, VI, KO, VO> then(Function<KI, KO> keyMapper,
                                                               Function<Option<VI>, VO> valueMapper) {
        return OrMatchClause2.of(() -> MatchResult2.of(subject()._2()
                                                                .map(kiViOptTuple -> kiViOptTuple.bimap(keyMapper,
                                                                                                        valueMapper))
                                                                .toEither(Tuple2.of(subject()._1(),
                                                                                    Option.none()))));
    }

    default <KO, VO> OrMatchClause2<K, V, KI, VI, KO, VO> then(BiFunction<KI, Option<VI>, Tuple2<KO, VO>> mapper) {
        return OrMatchClause2.of(() -> MatchResult2.of(subject()._2()
                                                                .map(kiViOptionTuple -> mapper.apply(kiViOptionTuple._1(),
                                                                                                     kiViOptionTuple._2()))
                                                                .toEither(Tuple2.of(subject()._1(),
                                                                                    Option.none()))));
    }
}
