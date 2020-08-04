package io.github.anticipasean.ent.pattern;

import io.github.anticipasean.ent.iterator.TypeMatchingIterator;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class MatchClauseImpl<V> implements MatchClause<V> {

    private final V valueObject;

    public MatchClauseImpl(V valueObject) {
        this.valueObject = valueObject;
    }

    @Override
    public <I> MatchPredicate<V, I> ifOfType(Class<I> possibleType) {
        if (PatternMatching.isOfType(valueObject,
                                     possibleType)) {
            return new MatchPredicateImpl<>(valueObject,
                                            PatternMatching.tryDynamicCast(valueObject,
                                                                           possibleType)
                                                           .orElse(null),
                                            null);
        }
        return new MatchPredicateImpl<>(valueObject,
                                        null,
                                        null);
    }

    @Override
    public <E> MatchIterablePredicate<V, E> ifIterableOver(Class<E> elementType) {
        if (PatternMatching.isOfType(valueObject,
                                     Iterable.class)) {
            Iterable iterable = PatternMatching.tryDynamicCast(valueObject,
                                                               Iterable.class)
                                               .orElse(() -> Stream.empty()
                                                                   .iterator());
            return new MatchIterablePredicateImpl<>(valueObject,
                                                    () -> new TypeMatchingIterator<>(iterable.iterator(), elementType));
        }
        return new MatchIterablePredicateImpl<>(valueObject, null);
    }

    @Override
    public ThenClause<V, V> ifFits(Predicate<V> condition) {
        if (Objects.requireNonNull(condition,
                                   "condition")
                   .test(valueObject)) {
            return new ThenClauseImpl<>(valueObject,
                                        valueObject);
        }
        return new ThenClauseImpl<>(valueObject,
                                    null);
    }

}
