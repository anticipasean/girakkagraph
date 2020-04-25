package io.github.anticipasean.ent.pattern;

public interface IfMatchClause<X> {

    <I> IfMatchPredicate<X, I> ifOfType(Class<I> possibleType);

}
