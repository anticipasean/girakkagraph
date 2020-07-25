package io.github.anticipasean.ent.pattern;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import cyclops.data.tuple.Tuple2;
import java.util.function.BiFunction;

public class ThenClause2Impl<K, V, I> implements ThenClause2<K, V, I> {

    private final Tuple2<K, V> tuple;
    private final I valueAsInputType;

    public ThenClause2Impl(Tuple2<K, V> tuple,
                           I valueAsInputType) {
        this.tuple = requireNonNull(tuple,
                                    "tuple");
        this.valueAsInputType = valueAsInputType;
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
