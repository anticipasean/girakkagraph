package io.github.anticipasean.ent.pattern;

public class IfNotMatchClauseImpl<E, I, O> implements IfNotMatchClause<E, I, O> {

    private final E eventObject;
    private final O resultOutput;

    public IfNotMatchClauseImpl(E eventObject,
                                O resultOutput) {
        this.eventObject = eventObject;
        this.resultOutput = resultOutput;
    }

    @Override
    public <I> IfNotMatchPredicate<E, I, O> ifOfType(Class<I> possibleType) {
        Pattern.logger.info("if_not_matches_type: state: obj {}, matches_type {}, result_output {}",
                            eventObject,
                            Pattern.isOfType(eventObject,
                                             possibleType),
                            resultOutput);
        if (resultOutput != null) {
            return new IfNotMatchPredicateImpl<>(eventObject,
                                                 null,
                                                 resultOutput);
        } else if (Pattern.isOfType(eventObject,
                                    possibleType)) {
            return new IfNotMatchPredicateImpl<>(eventObject,
                                                 possibleType,
                                                 null);
        }
        return new IfNotMatchPredicateImpl<>(eventObject,
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
