package io.github.anticipasean.ent.pattern;

import java.util.function.Function;

public interface ThenClause<E, I> {

    <O> IfNotMatchClause<E, I, O> then(Function<I, O> func);

}
