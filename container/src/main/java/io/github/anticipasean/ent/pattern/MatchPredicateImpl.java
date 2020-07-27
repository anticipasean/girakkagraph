package io.github.anticipasean.ent.pattern;

import cyclops.companion.Streamable;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public class MatchPredicateImpl<V, I> implements MatchPredicate<V, I> {

    private final V valueObject;
    private final I valueAsInputType;
    private final Iterator<I> valueAsIteratorOfInputType;

    public MatchPredicateImpl(V valueObject,
                              I valueAsInputType,
                              Iterator<I> valueAsIteratorOfInputType) {
        this.valueObject = valueObject;
        this.valueAsInputType = valueAsInputType;
        this.valueAsIteratorOfInputType = valueAsIteratorOfInputType;
    }

    @Override
    public ThenClause<V, I> and(Predicate<I> condition) {
        if (valueAsIteratorOfInputType != null && Objects.nonNull(condition) && condition.test(valueAsInputType)) {
            return new ThenClauseImpl<>(valueObject,
                                        valueAsInputType);
        }
        return new ThenClauseImpl<>(valueObject,
                                    null);
    }


    @Override
    public <O> OrMatchClause<V, I, O> then(Function<I, O> mapper) {
        if (valueAsInputType != null) {
            O resultOutput = Objects.requireNonNull(mapper,
                                                    () -> "mapper")
                                    .apply(valueAsInputType);
            return new OrMatchClauseImpl<V, I, O>(valueObject,
                                                  resultOutput);
        }
        return new OrMatchClauseImpl<>(valueObject,
                                       null);
    }
}
