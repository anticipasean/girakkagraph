package io.github.anticipasean.ent.pattern;

import cyclops.data.tuple.Tuple2;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface PatternMatching {

    static Logger logger = LoggerFactory.getLogger(PatternMatching.class);

    static <S> MatchClause<S> forValue(S someObject) {
        return new MatchClauseImpl<>(someObject);
    }

    static <K, V> MatchClause2<K, V> forKeyValuePair(K key,
                                                     V value) {
        return new MatchClause2Impl<>(Tuple2.of(key,
                                                value));
    }

    static <K, V> MatchClause2<K, V> forKeyValueTuple(Tuple2<K, V> tuple) {
        return new MatchClause2Impl<>(tuple);
    }

    static <K, V> BiFunction<K, V, MatchClause2<K, V>> keyValuePairPatternStarter() {
        return (k, v) -> new MatchClause2Impl<>(Tuple2.of(k,
                                                          v));
    }

    static <K, V> Function<Tuple2<K, V>, MatchClause2<K, V>> keyValueTuplePatternStarter() {
        return MatchClause2Impl::new;
    }

    static <V> Function<V, MatchClause<V>> singleValuePatternStarter() {
        return MatchClauseImpl::new;
    }

    static <I, R> boolean isOfType(I inputObject,
                                   Class<R> returnType) {
        return inputObject != null && (Objects.requireNonNull(returnType,
                                                              () -> "returnType specified may not be null")
                                              .isAssignableFrom(inputObject.getClass()) || (inputObject.getClass()
                                                                                                       .isPrimitive()
            && returnType.isInstance(inputObject)));
    }

    static <I, R> Optional<R> tryDynamicCast(I inputObject,
                                             Class<R> returnType) {
        try {
            return Optional.of(inputObject)
                           .map(input -> Objects.requireNonNull(returnType,
                                                                () -> "returnType specified may not be null for dynamic casting")
                                                .cast(input));
        } catch (ClassCastException e) {
            return Optional.empty();
        }
    }

}



