package io.github.anticipasean.ent.func;

import cyclops.function.Function1;

public interface Pattern1<V, O> extends Function1<MatchClause1<V>, O> {

    static <V, O> Function1<V, O> mapper(Pattern1<V, O> pattern1){
        return pattern1.compose(v -> MatchClause1.of(() -> v));
    }

}
