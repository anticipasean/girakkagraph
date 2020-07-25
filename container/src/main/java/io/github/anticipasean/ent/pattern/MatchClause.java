package io.github.anticipasean.ent.pattern;

import java.util.function.Predicate;

public interface MatchClause<X> {

    <I> MatchPredicate<X, I> ifOfType(Class<I> possibleType);

    ThenClause<X, X> ifFits(Predicate<X> condition);

}
