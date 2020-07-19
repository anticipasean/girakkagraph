package io.github.anticipasean.ent.pattern;

import java.util.Objects;
import java.util.function.Predicate;

public class MatchClauseImpl<E> implements MatchClause<E> {

    private final E eventObject;

    public MatchClauseImpl(E eventObject) {
        this.eventObject = eventObject;
    }

    @Override
    public <I> MatchPredicate<E, I> ifOfType(Class<I> possibleType) {
        PatternMatching.logger.info("if_matches_type: state: obj {}, matches_type {}",
                                    eventObject,
                                    PatternMatching.isOfType(eventObject,
                                                             possibleType));
        if (PatternMatching.isOfType(eventObject,
                                     possibleType)) {
            return new MatchPredicateImpl<>(eventObject,
                                            possibleType);
        }
        return new MatchPredicateImpl<>(eventObject,
                                        null);
    }

    @Override
    public ThenClause<E, E> ifFits(Predicate<E> condition) {
        if(Objects.requireNonNull(condition, "condition").test(eventObject)){
            return new ThenClauseImpl<>(eventObject, eventObject);
        }
        return new ThenClauseImpl<>(eventObject, null);
    }

}
