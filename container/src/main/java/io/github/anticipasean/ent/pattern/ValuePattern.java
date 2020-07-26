package io.github.anticipasean.ent.pattern;

import java.util.function.Function;

public interface ValuePattern<I, O> extends Function<MatchClause<I>, O> {

    static <T> Function<T, MatchClause<T>> starter(){
        return PatternMatching::forValue;
    }

    static <I, O> Function<I, O> mapper(Function<MatchClause<I>, O> pattern){
        return pattern.compose(starter());
    }


}
