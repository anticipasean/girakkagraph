package io.github.anticipasean.ent.pattern.single;

import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import io.github.anticipasean.ent.pattern.Clause;
import java.util.function.Function;
import java.util.function.Supplier;

public interface ThenOptionClause1<V, I> extends Clause<Tuple2<V, Option<Option<I>>>> {

    static <V, I> ThenOptionClause1<V, I> of(Supplier<Tuple2<V, Option<Option<I>>>> tupleSupplier) {
        return new ThenOptionClause1<V, I>() {
            @Override
            public Tuple2<V, Option<Option<I>>> get() {
                return tupleSupplier.get();
            }
        };
    }

    default <O> OrMatchClause1<V, I, O> then(Function<Option<I>, O> mapper) {
        return OrMatchClause1.of(() -> MatchResult1.of(subject()._2()
                                                                .toEither(Tuple2.of(subject()._1(),
                                                                                    Option.<I>none()))
                                                                .map(mapper)));
    }

}
