package io.github.anticipasean.ent.state;

import cyclops.control.Option;
import cyclops.data.ImmutableMap;
import cyclops.data.tuple.Tuple2;
import io.github.anticipasean.ent.Ent;
import io.github.anticipasean.ent.pattern.MatchClause;
import io.github.anticipasean.ent.pattern.Pattern;
import io.github.anticipasean.ent.pattern.PatternMatching;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;

public class ManyAttrEnt<ID, C> implements Ent<ID, C> {

    private final ImmutableMap<ID, C> data;

    //    protected TreeMap<Key<ID, C>, Attribute<C>> data;

    //    protected static <ID extends Comparable<ID>, C> TreeMap<Key<ID, C>, Attribute<C>> emptyDataMap() {
    ////        return TreeMap.<Key<ID, C>, Attribute<C>>empty(Key.keyComparator());
    //    }


    public ManyAttrEnt(ImmutableMap<ID, C> data) {
        this.data = data;
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public <R> Ent<ID, R> map(Function<? super C, ? extends R> fn) {
        Objects.requireNonNull(fn,
                               "fn");
        return new ManyAttrEnt<ID, R>(data.stream()
                                          .map(tuple -> tuple.map2(fn))
                                          .toHashMap(Tuple2::_1,
                                                     Tuple2::_2));
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
        return new ManyAttrEnt<ID, R>(data.mapValues(patternFunc.compose(PatternMatching.of()))
                                          .toHashMap(Tuple2::_1,
                                                     Tuple2::_2));

    }

    @Override
    public Iterator<Tuple2<ID, C>> iterator() {
        return data.iterator();
    }
}
