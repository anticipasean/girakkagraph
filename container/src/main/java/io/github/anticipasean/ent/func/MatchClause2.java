package io.github.anticipasean.ent.func;

import cyclops.data.tuple.Tuple2;
import java.util.function.Supplier;

public interface MatchClause2<K, V> extends Clause2<K, V> {

    static <K, V> MatchClause2<K, V> of(Supplier<Tuple2<K, V>> valueSupplier) {
        return new MatchClause2<K, V>() {
            @Override
            public Tuple2<K, V> get() {
                return valueSupplier.get();
            }
        };
    }



}
