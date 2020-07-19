package io.github.anticipasean.ent.pattern;

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
    public O orElse(O defaultOutput) {
        if (resultOutput != null) {
            return resultOutput;
        }
        return defaultOutput;
    }

    @Override
    public <X extends Throwable> O orElseThrow(X throwable) throws X {
        if (resultOutput != null) {
            return resultOutput;
        }
        throw throwable;
    }

}
