package io.github.anticipasean.ent.func;

import cyclops.companion.Streamable;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import java.util.function.Function;
import java.util.function.Supplier;

public interface ThenIterableClause1<V, I> extends Clause1<Tuple2<V, Option<Iterable<I>>>> {

    static <V, I, O> ThenIterableClause1<V, I> of(Supplier<Tuple2<V, Option<Iterable<I>>>> valueSupplier) {
        return new ThenIterableClause1<V, I>() {
            @Override
            public Tuple2<V, Option<Iterable<I>>> get() {
                return valueSupplier.get();
            }
        };
    }

    default <O> OrMatchClause1<V, I, O> then(Function<Streamable<I>, O> mapper) {
        return OrMatchClause1.of(() -> MatchResult.of(subject().map2(inputTypeAsOpt -> inputTypeAsOpt.map(Streamable::fromIterable)
                                                                                                     .map(mapper)
                                                                                                     .toEither(Tuple2.of(subject()._1(),
                                                                                                                         Option.<I>none())))
                                                               ._2()));
    }

}
