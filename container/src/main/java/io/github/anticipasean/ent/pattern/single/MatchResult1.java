package io.github.anticipasean.ent.pattern.single;

import com.oath.cyclops.matching.Deconstruct.Deconstruct3;
import cyclops.control.Either;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;

public interface MatchResult1<V, I, O> extends Deconstruct3<V, I, O> {

    static <V, I, O> MatchResult1<V, I, O> of(Either<Tuple2<V, Option<I>>, O> valueInputTupleOrResultObject) {
        return new MatchResult1<V, I, O>() {
            @Override
            public Either<Tuple2<V, Option<I>>, O> either() {
                return valueInputTupleOrResultObject;
            }
        };
    }

    Either<Tuple2<V, Option<I>>, O> either();

    @Override
    default Tuple3<V, I, O> unapply() {
        return Tuple3.of(either().leftOrElse(null)
                                 ._1(),
                         either().leftOrElse(null)
                                 ._2()
                                 .orElse(null),
                         either().orElse(null));
    }
}
