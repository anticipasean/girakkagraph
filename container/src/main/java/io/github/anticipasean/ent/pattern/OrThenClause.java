package io.github.anticipasean.ent.pattern;

import java.util.function.Function;

public interface OrThenClause<E, I, O> {

    OrMatchClause<E, I, O> then(Function<I, O> func);

}
