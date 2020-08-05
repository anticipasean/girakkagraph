package io.github.anticipasean.ent.pattern.single;

import cyclops.companion.Streamable;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import io.github.anticipasean.ent.pattern.Clause;
import java.util.function.Function;
import java.util.function.Supplier;

public interface ThenIterableClause1<V, I> extends Clause<Tuple2<V, Option<Iterable<I>>>> {

    static <V, I, O> ThenIterableClause1<V, I> of(Supplier<Tuple2<V, Option<Iterable<I>>>> valueSupplier) {
        return new ThenIterableClause1<V, I>() {
            @Override
            public Tuple2<V, Option<Iterable<I>>> get() {
                return valueSupplier.get();
            }
        };
    }

    default <O> OrMatchClause1<V, I, O> then(Function<Streamable<I>, O> mapper) {
        return OrMatchClause1.of(() -> MatchResult1.of(subject().map2(inputTypeAsOpt -> inputTypeAsOpt.map(iter -> Streamable.fromIterable(iter))
                                                                                                      .map(mapper))
                                                                ._2()
                                                                .toEither(Tuple2.of(subject()._1(),
                                                                                    Option.none()))));
    }

}
