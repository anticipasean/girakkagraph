package io.github.anticipasean.ent.pattern;

import java.util.function.Function;

public interface NextThenClause<E, I, O> {

    OrMatchClause<E, I, O> then(Function<I, O> func);

}
