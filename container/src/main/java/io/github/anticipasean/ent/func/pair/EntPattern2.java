package io.github.anticipasean.ent.func.pair;

import cyclops.data.tuple.Tuple2;
import cyclops.function.Function1;
import cyclops.function.Function2;
import io.github.anticipasean.ent.Ent;
import io.github.anticipasean.ent.func.Matcher;
import io.github.anticipasean.ent.func.Matcher.Matcher2;

public interface EntPattern2<K, V, KO, VO> extends Function1<Matcher2<K, V>, Ent<KO, VO>> {

    static <K, V, KO, VO> Function1<Tuple2<K, V>, Ent<KO, VO>> asMapper(EntPattern2<K, V, KO, VO> pattern2) {
        return pattern2.compose(Matcher::of);
    }

    static <K, V, KO, VO> Function2<K, V, Ent<KO, VO>> asBiMapper(EntPattern2<K, V, KO, VO> pattern2) {
        return (k, v) -> pattern2.apply(Matcher.of(k,
                                                   v));
    }


    @Override
    Ent<KO, VO> apply(Matcher2<K, V> matcher);
}
