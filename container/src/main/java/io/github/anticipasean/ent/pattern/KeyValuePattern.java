package io.github.anticipasean.ent.pattern;

import cyclops.data.tuple.Tuple2;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface KeyValuePattern<K, V, O> extends Function<MatchClause2<K, V>, Tuple2<K, O>> {

    static <K, V> BiFunction<K, V, MatchClause2<K, V>> pairStarter() {
        return PatternMatching::forKeyValuePair;
    }

    static <K, V> Function<Tuple2<K, V>, MatchClause2<K, V>> tupleStarter() {
        return PatternMatching::forKeyValueTuple;
    }

    static <K, V, O> Function<Tuple2<K, V>, Tuple2<K, O>> tupleMapper(Function<MatchClause2<K, V>, Tuple2<K, O>> pattern) {
        return pattern.compose(tupleStarter());
    }

    static <K, V, O> BiFunction<K, V, Tuple2<K, O>> pairMapper(Function<MatchClause2<K, V>, Tuple2<K, O>> pattern) {
        return (k, v) -> pattern.apply(PatternMatching.forKeyValueTuple(Tuple2.<K, V>of(k,
                                                                                        v)));

    }

}
