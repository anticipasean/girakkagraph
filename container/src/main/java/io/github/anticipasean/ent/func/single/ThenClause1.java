package io.github.anticipasean.ent.func.single;

import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import io.github.anticipasean.ent.func.Clause;
import java.util.function.Function;
import java.util.function.Supplier;
import org.slf4j.LoggerFactory;

public interface ThenClause1<V, I> extends Clause<Tuple2<V, Option<I>>> {

    static <V, I, O> ThenClause1<V, I> of(Supplier<Tuple2<V, Option<I>>> valueSupplier) {
        return new ThenClause1<V, I>() {
            @Override
            public Tuple2<V, Option<I>> get() {
                return valueSupplier.get();
            }
        };
    }

    default <O> OrMatchClause1<V, I, O> then(Function<I, O> mapper) {
        LoggerFactory.getLogger(ThenClause1.class).info("current_state: " + subject().toString());
        return OrMatchClause1.of(() -> MatchResult1.of(subject().map2(inputTypeAsOpt -> inputTypeAsOpt.map(mapper)
                                                                                                      .toEither(Tuple2.of(subject()._1(),
                                                                                                                    Option.<I>none())))
                                                                ._2()));
    }

}
