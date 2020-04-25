package io.github.anticipasean.ent.pattern;

public class IfMatchClauseImpl<E> implements IfMatchClause<E> {

    private final E eventObject;

    public IfMatchClauseImpl(E eventObject) {
        this.eventObject = eventObject;
    }

    @Override
    public <I> IfMatchPredicate<E, I> ifOfType(Class<I> possibleType) {
        Pattern.logger.info("if_matches_type: state: obj {}, matches_type {}",
                            eventObject,
                            Pattern.isOfType(eventObject,
                                             possibleType));
        if (Pattern.isOfType(eventObject,
                             possibleType)) {
            return new IfMatchPredicateImpl<>(eventObject,
                                              possibleType);
        }
        return new IfMatchPredicateImpl<>(eventObject,
                                          null);
    }

}
