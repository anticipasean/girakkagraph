package io.github.anticipasean.ent.func;

import cyclops.companion.Streamable;
import java.util.function.Function;

public interface ThenIterableClause<V, I>  {

    <O> OrMatchClause1<V, I, O> then(Function<Streamable<I>, O> mapper);

}
