package io.github.anticipasean.ent.state;

import cyclops.control.Option;
import cyclops.data.HashMap;
import cyclops.data.ImmutableMap;
import cyclops.data.tuple.Tuple2;
import io.github.anticipasean.ent.Ent;
import io.github.anticipasean.ent.pattern.MatchClause;
import io.github.anticipasean.ent.pattern.Pattern;
import io.github.anticipasean.ent.pattern.PatternMatching;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;

public class SingleAttrEnt<ID, C> implements Ent<ID, C> {

    private final ImmutableMap<ID, C> data;

    public SingleAttrEnt(ID id,
                         C container) {
        data = HashMap.of(id,
                          container);
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public <R> Ent<ID, R> map(Function<? super C, ? extends R> fn) {
        Objects.requireNonNull(fn,
                               "fn");
        return data.stream()
                   .map(tuple -> (Ent<ID, R>) new SingleAttrEnt<ID, R>(tuple._1(),
                                                                       fn.apply(tuple._2())))
                   .singleOrElse(EmptyAttrEnt.<ID, R>emptyEnt());
    }

    @Override
    public <R> Option<R> getAndMatch(ID id,
                                     Function<MatchClause<C>, R> patternMap) {
        Objects.requireNonNull(patternMap,
                               "patternMap");
        return data.get(id)
                   .map(patternMap.compose(PatternMatching::forObject));
    }

    @Override
    public <R> Ent<ID, R> mapWithPattern(Pattern<C, R> patternFunc) {
        return data.mapValues(patternFunc.compose(PatternMatching.of()))
                   .single()
                   .map(idrTuple2 -> (Ent<ID, R>) new SingleAttrEnt<ID, R>(idrTuple2._1(),
                                                                           idrTuple2._2()))
                   .orElse(EmptyAttrEnt.emptyEnt());
    }


    @Override
    public Iterator<Tuple2<ID, C>> iterator() {
        return data.iterator();
    }
}
