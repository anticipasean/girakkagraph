package io.github.anticipasean.ent.pattern;

import java.util.function.Function;

public interface Pattern<I, O> extends Function<MatchClause<I>, O> {



}
