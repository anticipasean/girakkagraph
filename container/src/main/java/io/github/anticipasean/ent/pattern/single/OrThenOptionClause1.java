package io.github.anticipasean.ent.pattern.single;

import cyclops.control.Either;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import io.github.anticipasean.ent.pattern.Clause;
import java.util.function.Function;
import java.util.function.Supplier;

public interface OrThenOptionClause1<V, I, O> extends Clause<MatchResult1<V, Option<I>, O>> {

    static <V, I, O> OrThenOptionClause1<V, I, O> of(Supplier<MatchResult1<V, Option<I>, O>> matchResult1Supplier) {
        return new OrThenOptionClause1<V, I, O>() {
            @Override
            public MatchResult1<V, Option<I>, O> get() {
                return matchResult1Supplier.get();
            }
        };
    }

    default OrMatchClause1<V, I, O> then(Function<Option<I>, O> mapper) {
        return OrMatchClause1.of(() -> MatchResult1.of(subject().either()
                                                                .mapLeft(Tuple2::_2)
                                                                .mapLeft(optOpt -> optOpt.map(mapper))
                                                                .fold(oOpt -> oOpt.toEither(Tuple2.of(subject().unapply()
                                                                                                               ._1(),
                                                                                                      Option.none())),
                                                                      Either::right)));
    }
}
