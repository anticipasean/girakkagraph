package io.github.anticipasean.ent.pattern;

import java.util.Objects;
import java.util.function.Predicate;

public class MatchClauseImpl<X> implements MatchClause<X> {

    private final X eventObject;

    public MatchClauseImpl(X eventObject) {
        this.eventObject = eventObject;
    }

    @Override
    public <I> MatchPredicate<X, I> ifOfType(Class<I> possibleType) {
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
    public ThenClause<X, X> ifFits(Predicate<X> condition) {
        if (Objects.requireNonNull(condition,
                                   "condition")
                   .test(eventObject)) {
            return new ThenClauseImpl<>(eventObject,
                                        eventObject);
        }
        return new ThenClauseImpl<>(eventObject,
                                    null);
    }



}
