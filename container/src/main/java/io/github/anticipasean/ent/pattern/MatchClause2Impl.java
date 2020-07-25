package io.github.anticipasean.ent.pattern;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import cyclops.data.tuple.Tuple2;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class MatchClause2Impl<K, V> implements MatchClause2<K, V> {

    private final Tuple2<K, V> tuple;

    public MatchClause2Impl(Tuple2<K, V> tuple) {
        this.tuple = requireNonNull(tuple,
                                    "tuple");
    }

    @Override
    public <I> MatchPredicate2<K, V, I> ifKeyFitsAndValueOfType(Predicate<K> condition,
                                                                Class<I> possibleType) {
        if (nonNull(condition) && condition.test(tuple._1()) && nonNull(possibleType) && PatternMatching.isOfType(tuple._2(),
                                                                                                                  possibleType)) {
            Optional<I> valueAsMatchingTypeOpt = PatternMatching.tryDynamicCast(tuple._2(),
                                                                                possibleType);
            if (valueAsMatchingTypeOpt.isPresent()) {
                return new MatchPredicate2Impl<>(tuple,
                                                 valueAsMatchingTypeOpt.get());
            }
        }
        return new MatchPredicate2Impl<>(tuple,
                                         null);
    }

    @Override
    public ThenClause2<K, V, V> ifKeyValueFits(BiPredicate<K, V> condition) {
        if (nonNull(condition) && condition.test(tuple._1(),
                                                 tuple._2())) {
            return new ThenClause2Impl<>(tuple,
                                         tuple._2());
        }
        return new ThenClause2Impl<>(tuple,
                                     null);
    }
}
