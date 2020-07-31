package io.github.anticipasean.ent.func;

import java.util.function.Function;

public interface Matcher {

    static <V> Function<V, MatchClause1<V>> starter() {
        return v -> caseWhen(v);
    }

    static <V> MatchClause1<V> caseWhen(V value) {
        return MatchClause1.of(() -> value);
    }

}
