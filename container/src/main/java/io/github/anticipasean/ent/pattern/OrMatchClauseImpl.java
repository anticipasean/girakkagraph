package io.github.anticipasean.ent.pattern;

import cyclops.control.Option;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class OrMatchClauseImpl<E, I, O> implements OrMatchClause<E, I, O> {

    private final E eventObject;
    private final O resultOutput;

    public OrMatchClauseImpl(E eventObject,
                             O resultOutput) {
        this.eventObject = eventObject;
        this.resultOutput = resultOutput;
    }

    @Override
    public <I> OrMatchPredicate<E, I, O> ifOfType(Class<I> possibleType) {
        PatternMatching.logger.info("if_not_matches_type: state: obj {}, matches_type {}, result_output {}",
                                    eventObject,
                                    PatternMatching.isOfType(eventObject,
                                                             possibleType),
                                    resultOutput);
        if (resultOutput != null) {
            return new OrMatchPredicateImpl<>(eventObject,
                                              null,
                                              resultOutput);
        } else if (PatternMatching.isOfType(eventObject,
                                            possibleType)) {
            return new OrMatchPredicateImpl<>(eventObject,
                                              possibleType,
                                              null);
        }
        return new OrMatchPredicateImpl<>(eventObject,
                                          null,
                                          null);
    }

    @Override
    public NextThenClause<E, E, O> ifFits(Predicate<E> condition) {
        if (resultOutput != null) {
            return new NextThenClauseImpl<>(eventObject,
                                            null,
                                            resultOutput);
        }
        if (Objects.requireNonNull(condition,
                                   "condition")
                   .test(eventObject)) {
            return new NextThenClauseImpl<>(eventObject,
                                            eventObject,
                                            null);
        }
        return new NextThenClauseImpl<>(eventObject,
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
