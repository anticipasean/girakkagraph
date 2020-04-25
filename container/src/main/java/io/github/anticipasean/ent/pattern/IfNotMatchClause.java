package io.github.anticipasean.ent.pattern;

public interface IfNotMatchClause<E, I, O> {

    <I> IfNotMatchPredicate<E, I, O> ifOfType(Class<I> possibleType);

    O orElse(O defaultOutput);

    <X extends Throwable> O orElseThrow(X throwable) throws X;

}
