package io.github.anticipasean.ent.pattern.pair;

import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import cyclops.function.Function1;
import cyclops.function.Function2;
import cyclops.reactive.ReactiveSeq;
import io.github.anticipasean.ent.pattern.Matcher;
import io.github.anticipasean.ent.pattern.Matcher.Matcher2;

public interface Pattern2<K, V, KO, VO> extends Function1<Matcher2<K, V>, Tuple2<KO, VO>> {

    static <K, V, KO, VO> Function1<Tuple2<K, V>, Tuple2<KO, VO>> asMapper(Function1<Matcher2<K, V>, Tuple2<KO, VO>> pattern2) {
        return pattern2.compose(Matcher::of);
    }

    static <K, V, KO, VO> Function2<K, V, Tuple2<KO, VO>> asBiMapper(Function1<Matcher2<K, V>, Tuple2<KO, VO>> pattern2) {
        return (k, v) -> pattern2.apply(Matcher.of(k,
                                                   v));
    }

    static <K, V, KO, VO> Function2<K, V, ? extends Iterable<Tuple2<KO, VO>>> asConcatMapper(Function1<Matcher2<K, V>, Tuple2<? extends Iterable<KO>, ? extends Iterable<VO>>> pattern2) {
        return (k, v) -> Option.of(pattern2.apply(Matcher.of(k,
                                                             v)))
                               .fold(tupleIterable -> ReactiveSeq.fromIterable(tupleIterable._1())
                                                                 .zip(tupleIterable._2()),
                                     ReactiveSeq::empty);
    }

    @Override
    Tuple2<KO, VO> apply(Matcher2<K, V> matcher);
}
