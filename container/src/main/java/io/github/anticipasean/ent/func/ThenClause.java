package io.github.anticipasean.ent.func;

import io.github.anticipasean.ent.func.single.OrMatchClause1;
import java.util.function.Function;

public interface ThenClause<V, I> {

    <O> OrMatchClause1<V, I, O> then(Function<I, O> mapper);

}
