package io.github.anticipasean.ent.pattern;

public interface MatchClause<X> {

    <I> MatchPredicate<X, I> ifOfType(Class<I> possibleType);

}
