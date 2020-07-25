package io.github.anticipasean.ent.pattern;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import cyclops.data.tuple.Tuple2;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class OrMatchPredicate2Impl<K, V, I, O> implements OrMatchPredicate2<K, V, I, O> {

    private final Tuple2<K, V> tuple;
    private final I valueAsMatchedType;
    private final O resultOutput;

    public OrMatchPredicate2Impl(Tuple2<K, V> tuple,
                                 I valueAsMatchedType,
                                 O resultOutput) {
        this.tuple = requireNonNull(tuple,
                                    "tuple");
        this.valueAsMatchedType = valueAsMatchedType;
        this.resultOutput = resultOutput;
    }

    @Override
    public OrThenClause2<K, V, I, O> and(Predicate<I> condition) {
        if (nonNull(resultOutput)) {
            return new OrThenClause2Impl<>(tuple,
                                           null,
                                           resultOutput);
        } else if (nonNull(valueAsMatchedType) && nonNull(condition) && condition.test(valueAsMatchedType)) {
            return new OrThenClause2Impl<>(tuple,
                                           valueAsMatchedType,
                                           null);
        } else {
            return new OrThenClause2Impl<>(tuple,
                                           null,
                                           null);
        }
    }

    @Override
    public OrMatchClause2<K, V, O> then(BiFunction<K, I, O> keyValueMapper) {
        if (nonNull(resultOutput)) {
            return new OrMatchClause2Impl<>(tuple,
                                            resultOutput);
        } else if (nonNull(valueAsMatchedType) && nonNull(keyValueMapper)) {
            return new OrMatchClause2Impl<>(tuple,
                                            keyValueMapper.apply(tuple._1(),
                                                                 valueAsMatchedType));
        } else {
            return new OrMatchClause2Impl<>(tuple,
                                            null);
        }
    }
}
