package io.github.anticipasean.ent.pattern;

public interface OrMatchClause<E, I, O> {

    <I> OrMatchPredicate<E, I, O> ifOfType(Class<I> possibleType);

    O orElse(O defaultOutput);

    <X extends Throwable> O orElseThrow(X throwable) throws X;

}
