package io.github.anticipasean.ent.func.pair;

import com.oath.cyclops.matching.Deconstruct.Deconstruct3;
import cyclops.control.Either;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;


public interface MatchResult2<K, V, KI, VI, KO, VO> extends Deconstruct3<Tuple2<K, V>, Option<Tuple2<KI, VI>>, Tuple2<KO, VO>> {


    static <K, V, KI, VI, KO, VO> MatchResult2<K, V, KI, VI, KO, VO> of(Either<Tuple2<Tuple2<K, V>, Option<Tuple2<KI, VI>>>, Tuple2<KO, VO>> inputValuesOrResultOutputEither) {
        return new MatchResult2<K, V, KI, VI, KO, VO>() {
            @Override
            public Either<Tuple2<Tuple2<K, V>, Option<Tuple2<KI, VI>>>, Tuple2<KO, VO>> either() {
                return inputValuesOrResultOutputEither;
            }
        };
    }

    Either<Tuple2<Tuple2<K, V>, Option<Tuple2<KI, VI>>>, Tuple2<KO, VO>> either();

    @Override
    default Tuple3<Tuple2<K, V>, Option<Tuple2<KI, VI>>, Tuple2<KO, VO>> unapply() {
        return Tuple3.of(either().leftOrElse(null)
                                 ._1(),
                         either().leftOrElse(null)
                                 ._2(),
                         either().orElse(null));
    }
}
