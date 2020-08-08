package io.github.anticipasean.ent.pattern.pair;

import cyclops.control.Either;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import io.github.anticipasean.ent.pattern.Clause;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public interface OrThenOptionClause2<K, V, KI, VI, KO, VO> extends Clause<MatchResult2<K, V, KI, Option<VI>, KO, VO>> {

    static <K, V, KI, VI, KO, VO> OrThenOptionClause2<K, V, KI, VI, KO, VO> of(Supplier<MatchResult2<K, V, KI, Option<VI>, KO, VO>> keyValueMatchResultsSupplier) {
        return new OrThenOptionClause2<K, V, KI, VI, KO, VO>() {
            @Override
            public MatchResult2<K, V, KI, Option<VI>, KO, VO> get() {
                return keyValueMatchResultsSupplier.get();
            }
        };
    }

    default OrMatchClause2<K, V, KI, VI, KO, VO> then(Function<Tuple2<KI, Option<VI>>, Tuple2<KO, VO>> mapper) {
        return OrMatchClause2.of(() -> MatchResult2.of(subject().either()
                                                                .mapLeft(Tuple2::_2)
                                                                .mapLeft(kiViOptTuple -> kiViOptTuple.map(mapper))
                                                                .fold(kovoTupleOpt -> kovoTupleOpt.toEither(Tuple2.of(subject().unapply()
                                                                                                                               ._1(),
                                                                                                                      Option.none())),
                                                                      kovoTuple2 -> Either.right(kovoTuple2))));
    }

    default OrMatchClause2<K, V, KI, VI, KO, VO> then(BiFunction<KI, Option<VI>, Tuple2<KO, VO>> biMapper) {
        return OrMatchClause2.of(() -> MatchResult2.of(subject().either()
                                                                .mapLeft(Tuple2::_2)
                                                                .mapLeft(kiviOptTuple -> kiviOptTuple.map(kiviTuple -> biMapper.apply(kiviTuple._1(),
                                                                                                                                      kiviTuple._2())))
                                                                .fold(kovoTupleOpt -> kovoTupleOpt.toEither(Tuple2.of(subject().unapply()
                                                                                                                               ._1(),
                                                                                                                      Option.none())),
                                                                      kovoTuple2 -> Either.right(kovoTuple2))));
    }

    default OrMatchClause2<K, V, KI, VI, KO, VO> then(Function<KI, KO> keyMapper,
                                                      Function<Option<VI>, VO> valueMapper) {
        return OrMatchClause2.of(() -> MatchResult2.of(subject().either()
                                                                .mapLeft(Tuple2::_2)
                                                                .mapLeft(kiviOptTuple -> kiviOptTuple.map(kiviTuple -> kiviTuple.bimap(keyMapper,
                                                                                                                                       valueMapper)))
                                                                .fold(kovoTupleOpt -> kovoTupleOpt.toEither(Tuple2.of(subject().unapply()
                                                                                                                               ._1(),
                                                                                                                      Option.none())),
                                                                      kovoTuple2 -> Either.right(kovoTuple2))));
    }
}
