package io.github.anticipasean.ent.func.single;

import cyclops.companion.Streamable;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import io.github.anticipasean.ent.func.Clause;
import java.util.function.Function;
import java.util.function.Supplier;

public interface OrThenIterableClause1<V, I, O> extends Clause<MatchResult1<V, Iterable<I>, O>> {

    static <V, I, O> OrThenIterableClause1<V, I, O> of(Supplier<MatchResult1<V, Iterable<I>, O>> supplier) {
        return new OrThenIterableClause1<V, I, O>() {
            @Override
            public MatchResult1<V, Iterable<I>, O> get() {
                return supplier.get();
            }
        };
    }

    default OrMatchClause1<V, I, O> then(Function<Streamable<I>, O> mapper) {
        return OrMatchClause1.of(() -> MatchResult1.of(subject().either()
                                                                .mapLeft(Tuple2::_2)
                                                                .mapLeft(valueAsInputTypeOpt -> valueAsInputTypeOpt.map(Streamable::fromIterable)
                                                                                                              .map(mapper))
                                                                .flatMapLeft(outputTypeOpt -> outputTypeOpt.toEither(Tuple2.of(subject().either()
                                                                                                                               .leftOrElse(null)
                                                                                                                               ._1(),
                                                                                                                          Option.none())))));
    }

}
