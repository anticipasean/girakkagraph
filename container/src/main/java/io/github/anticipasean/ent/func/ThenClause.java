package io.github.anticipasean.ent.func;

import java.util.function.Function;

public interface ThenClause<V, I> {

    <O> OrMatchClause1<V, I, O> then(Function<I, O> mapper);

}
