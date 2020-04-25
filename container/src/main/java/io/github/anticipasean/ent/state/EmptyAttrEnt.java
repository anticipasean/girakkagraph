package io.github.anticipasean.ent.state;

import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import io.github.anticipasean.ent.Ent;
import io.github.anticipasean.ent.pattern.IfMatchClause;
import java.util.Collections;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Supplier;

public final class EmptyAttrEnt<ID, C> implements Ent<ID, C> {


    @SuppressWarnings("unchecked")
    public static <ID, C> Ent<ID, C> emptyEnt() {
        return (Ent<ID, C>) EmptyEntSupplier.INSTANCE.get();
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public <R> Ent<ID, R> map(Function<? super C, ? extends R> fn) {
        return emptyEnt();
    }

    @Override
    public <R> Option<R> getAndMatch(ID id,
                                     Function<IfMatchClause<C>, R> patternMap) {
        return Option.none();
    }

    @Override
    public Iterator<Tuple2<ID, C>> iterator() {
        return Collections.emptyIterator();
    }

    private static enum EmptyEntSupplier implements Supplier<Ent<?,?>> {
        INSTANCE;

        private final EmptyAttrEnt<?,?> emptyEnt = new EmptyAttrEnt<>();

        @Override
        public Ent<?,?> get() {
            return (Ent<?,?>) emptyEnt;
        }
    }


}
