package io.github.anticipasean.ent.pattern;

import cyclops.companion.Streamable;
import java.util.function.Function;

public interface OrThenIterableClause<V, I, O> {

    OrMatchClause<V, I, O> then(Function<Streamable<I>, O> mapper);

}
