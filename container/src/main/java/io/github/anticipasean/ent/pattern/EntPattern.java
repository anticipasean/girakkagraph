package io.github.anticipasean.ent.pattern;

import io.github.anticipasean.ent.Ent;
import java.util.function.BiFunction;

public interface EntPattern<K, V, K2, V2> extends BiFunction<MatchClause<K>, MatchClause<V>, Ent<K2, V2>> {



}
