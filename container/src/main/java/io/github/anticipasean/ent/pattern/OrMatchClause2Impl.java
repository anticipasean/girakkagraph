package io.github.anticipasean.ent.pattern;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class OrMatchClause2Impl<K, V, O> implements OrMatchClause2<K, V, O> {

    private final Tuple2<K, V> tuple;
    private final O resultOutput;

    public OrMatchClause2Impl(Tuple2<K, V> tuple,
                              O resultOutput) {
        this.tuple = requireNonNull(tuple,
                                    "tuple");
        this.resultOutput = resultOutput;
    }

    @Override
    public <I> OrMatchPredicate2<K, V, I, O> ifKeyFitsAndValueOfType(Predicate<K> condition,
                                                                     Class<I> possibleType) {
        if (nonNull(resultOutput)) {
            return new OrMatchPredicate2Impl<>(tuple,
                                               null,
                                               resultOutput);
        } else if (nonNull(condition) && condition.test(tuple._1()) && PatternMatching.isOfType(tuple._2(),
                                                                                                possibleType)) {
            Optional<I> valueAsMatchedTypeOpt = PatternMatching.tryDynamicCast(tuple._2(),
                                                                               possibleType);
            if (valueAsMatchedTypeOpt.isPresent()) {
                return new OrMatchPredicate2Impl<>(tuple,
                                                   valueAsMatchedTypeOpt.get(),
                                                   null);
            }
        }
        return new OrMatchPredicate2Impl<>(tuple,
                                           null,
                                           null);

    }

    @Override
    public OrThenClause2<K, V, V, O> ifKeyValueFits(BiPredicate<K, V> condition) {
        if (nonNull(resultOutput)) {
            return new OrThenClause2Impl<>(tuple,
                                           null,
                                           resultOutput);
        } else if (nonNull(condition) && condition.test(tuple._1(),
                                                        tuple._2())) {
            return new OrThenClause2Impl<>(tuple,
                                           tuple._2(),
                                           null);
        }
        return new OrThenClause2Impl<>(tuple,
                                       null,
                                       null);
    }

    @Override
    public Option<O> get() {
        return Option.ofNullable(resultOutput);
    }

    @Override
    public O orElse(O defaultOutput) {
        return Option.ofNullable(resultOutput)
                     .orElse(defaultOutput);
    }

    @Override
    public O orElseGet(Supplier<O> defaultOutputSupplier) {
        return Option.ofNullable(resultOutput)
                     .orElseGet(defaultOutputSupplier);
    }

    @Override
    public <X extends RuntimeException> O orElseThrow(Supplier<X> throwableSupplier) {
        if (resultOutput != null) {
            return resultOutput;
        }
        throw throwableSupplier.get();
    }
}
