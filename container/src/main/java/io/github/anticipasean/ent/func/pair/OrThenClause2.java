package io.github.anticipasean.ent.func.pair;

import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import io.github.anticipasean.ent.func.Clause;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public interface OrThenClause2<K, V, KI, VI, KO, VO> extends Clause<MatchResult2<K, V, KI, VI, KO, VO>> {

    static <K, V, KI, VI, KO, VO> OrThenClause2<K, V, KI, VI, KO, VO> of(Supplier<MatchResult2<K, V, KI, VI, KO, VO>> keyValueMatchResultsSupplier) {
        return new OrThenClause2<K, V, KI, VI, KO, VO>() {
            @Override
            public MatchResult2<K, V, KI, VI, KO, VO> get() {
                return keyValueMatchResultsSupplier.get();
            }
        };
    }

    default OrMatchClause2<K, V, KI, VI, KO, VO> then(BiFunction<KI, VI, Tuple2<KO, VO>> biMapper) {
        return OrMatchClause2.of(() -> MatchResult2.of(subject().either()
                                                                .mapLeft(Tuple2::_2)
                                                                .mapLeft(kiviOptTuple -> kiviOptTuple.map(kiviTuple -> biMapper.apply(kiviTuple._1(),
                                                                                                                                    kiviTuple._2())))
                                                                .toEither(Tuple2.of(subject().either()
                                                                                             .leftOrElse(null)
                                                                                             ._1(),
                                                                                    Option.none()))));
    }

    default OrMatchClause2<K, V, KI, VI, KO, VO> then(Function<KI, KO> keyMapper,
                                                      Function<VI, VO> valueMapper) {
        return OrMatchClause2.of(() -> MatchResult2.of(subject().either()
                                                                .mapLeft(Tuple2::_2)
                                                                .mapLeft(kiviOptTuple -> kiviOptTuple.map(kiviTuple -> kiviTuple.bimap(keyMapper,
                                                                                                                                       valueMapper)))
                                                                .toEither(Tuple2.of(subject().either()
                                                                                             .leftOrElse(null)
                                                                                             ._1(),
                                                                                    Option.none()))));
    }

}
