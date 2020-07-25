package io.github.anticipasean.ent.pattern;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import cyclops.data.tuple.Tuple2;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class MatchPredicate2Impl<K, V, I> implements MatchPredicate2<K, V, I> {

    private final Tuple2<K, V> tuple;
    private final I valueAsInputType;

    public MatchPredicate2Impl(Tuple2<K, V> tuple,
                               I valueAsInputType) {
        this.tuple = requireNonNull(tuple,
                                    "tuple");
        this.valueAsInputType = valueAsInputType;
    }

    @Override
    public ThenClause2<K, V, I> andValueFits(Predicate<I> condition) {
        if (nonNull(valueAsInputType) && nonNull(condition) && condition.test(valueAsInputType)) {
            return new ThenClause2Impl<>(tuple,
                                         valueAsInputType);
        }
        return new ThenClause2Impl<>(tuple,
                                     null);
    }

    @Override
    public <O> OrMatchClause2<K, V, O> then(BiFunction<K, I, O> keyValueMapper) {
        if (nonNull(valueAsInputType) && nonNull(keyValueMapper)) {
            return new OrMatchClause2Impl<>(tuple,
                                            keyValueMapper.apply(tuple._1(),
                                                                 valueAsInputType));
        }
        return new OrMatchClause2Impl<>(tuple,
                                        null);
    }
}
