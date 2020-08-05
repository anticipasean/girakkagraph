package io.github.anticipasean.ent.pattern.single;

import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import io.github.anticipasean.ent.pattern.Clause;
import java.util.function.Function;
import java.util.function.Supplier;

public interface OrThenClause1<V, I, O> extends Clause<MatchResult1<V, I, O>> {

    static <V, I, O> OrThenClause1<V, I, O> of(Supplier<MatchResult1<V, I, O>> supplier) {
        return new OrThenClause1<V, I, O>() {
            @Override
            public MatchResult1<V, I, O> get() {
                return supplier.get();
            }
        };
    }

    default OrMatchClause1<V, I, O> then(Function<I, O> mapper) {
        return OrMatchClause1.of(() -> MatchResult1.of(subject().either()
                                                                .mapLeft(Tuple2::_2)
                                                                .mapLeft(valueAsInputTypeOpt -> valueAsInputTypeOpt.map(mapper))
                                                                .flatMapLeft(outputTypeOpt -> outputTypeOpt.toEither(Tuple2.of(subject().either()
                                                                                                                                        .leftOrElse(null)
                                                                                                                                        ._1(),
                                                                                                                               Option.none())))));
    }

}
