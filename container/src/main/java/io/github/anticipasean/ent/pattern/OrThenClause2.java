package io.github.anticipasean.ent.pattern;

import java.util.function.BiFunction;

public interface OrThenClause2<K, V, I, O> {

    OrMatchClause2<K, V, O> then(BiFunction<K, I, O> keyValueMapper);

}
