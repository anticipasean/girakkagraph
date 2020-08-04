package io.github.anticipasean.ent.pattern;

import static java.util.Objects.nonNull;

import cyclops.control.Option;
import io.github.anticipasean.ent.iterator.TypeMatchingIterable;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class OrMatchClauseImpl<V, I, O> implements OrMatchClause<V, I, O> {

    private final V valueObject;
    private final O resultOutput;

    public OrMatchClauseImpl(V valueObject,
                             O resultOutput) {
        this.valueObject = valueObject;
        this.resultOutput = resultOutput;
    }

    @Override
    public <I> OrMatchPredicate<V, I, O> ifOfType(Class<I> possibleType) {
        PatternMatching.logger.info("if_not_matches_type: state: obj {}, matches_type {}, result_output {}",
                                    valueObject,
                                    PatternMatching.isOfType(valueObject,
                                                             possibleType),
                                    resultOutput);
        if (resultOutput != null) {
            return new OrMatchPredicateImpl<>(valueObject,
                                              null,
                                              resultOutput);
        } else if (PatternMatching.isOfType(valueObject,
                                            possibleType)) {
            return new OrMatchPredicateImpl<>(valueObject,
                                              PatternMatching.tryDynamicCast(valueObject,
                                                                             possibleType)
                                                             .orElse(null),
                                              null);
        }
        return new OrMatchPredicateImpl<>(valueObject,
                                          null,
                                          null);
    }

    @Override
    public <E> OrMatchIterablePredicate<V, E, O> ifIterableOver(Class<E> elementType) {
        if (resultOutput != null) {
            return new OrMatchIterablePredicateImpl<>(valueObject,
                                                      null,
                                                      resultOutput);
        } else if (PatternMatching.isOfType(valueObject,
                                            Iterable.class) && nonNull(elementType)) {
            Iterable iterable = PatternMatching.tryDynamicCast(valueObject,
                                                               Iterable.class)
                                               .orElse(null);
            return new OrMatchIterablePredicateImpl<>(valueObject,
                                                      TypeMatchingIterable.of(iterable.iterator(),
                                                                           elementType),
                                                      null);
        } else {
            return new OrMatchIterablePredicateImpl<>(valueObject,
                                                      null,
                                                      null);
        }
    }

    @Override
    public OrThenClause<V, V, O> ifFits(Predicate<V> condition) {
        if (resultOutput != null) {
            return new OrThenClauseImpl<>(valueObject,
                                          null,
                                          resultOutput);
        }
        if (Objects.requireNonNull(condition,
                                   "condition")
                   .test(valueObject)) {
            return new OrThenClauseImpl<>(valueObject,
                                          valueObject,
                                          null);
        }
        return new OrThenClauseImpl<>(valueObject,
                                      null,
                                      null);
    }

    @Override
    public Option<O> get() {
        return Option.ofNullable(resultOutput);
    }

    @Override
    public O orElse(O defaultOutput) {
        if (resultOutput != null) {
            return resultOutput;
        }
        return defaultOutput;
    }

    @Override
    public O orElseGet(Supplier<O> defaultOutputSupplier) {
        if (resultOutput != null) {
            return resultOutput;
        }
        return Objects.requireNonNull(defaultOutputSupplier,
                                      "defaultOutputSupplier")
                      .get();
    }

    @Override
    public <X extends RuntimeException> O orElseThrow(Supplier<X> throwable) {
        if (resultOutput != null) {
            return resultOutput;
        }
        throw throwable.get();
    }

}
