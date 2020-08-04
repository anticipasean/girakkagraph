package io.github.anticipasean.ent.func.pair;

import cyclops.companion.Streamable;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import cyclops.function.Function1;
import cyclops.function.Function2;
import io.github.anticipasean.ent.func.Matcher;
import io.github.anticipasean.ent.func.Matcher.Matcher2;

public interface Pattern2<K, V, KO, VO> extends Function1<Matcher2<K, V>, Tuple2<KO, VO>> {

    static <K, V, KO, VO> Function1<Tuple2<K, V>, Tuple2<KO, VO>> asMapper(Pattern2<K, V, KO, VO> pattern2) {
        return pattern2.compose(Matcher::of);
    }

    static <K, V, KO, VO> Function2<K, V, Tuple2<KO, VO>> asBiMapper(Pattern2<K, V, KO, VO> pattern2) {
        return (k, v) -> pattern2.apply(Matcher.of(k,
                                                   v));
    }

    static <K, V, KO, VO> Function2<K, V, Iterable<Tuple2<KO, VO>>> asConcatMapper(Pattern2<K, V, Iterable<KO>, Iterable<VO>> pattern2) {
        return (k, v) -> Option.of(pattern2.apply(Matcher.of(k,
                                                             v)))
                               .map(koVoIterablesTuple -> Streamable.fromIterable(koVoIterablesTuple._1())
                                                                     .zip(koVoIterablesTuple._2())).orElseGet(Streamable::empty);
    }

    @Override
    Tuple2<KO, VO> apply(Matcher2<K, V> matcher);
}
