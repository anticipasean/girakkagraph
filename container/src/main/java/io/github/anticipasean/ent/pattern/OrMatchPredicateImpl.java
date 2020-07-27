package io.github.anticipasean.ent.pattern;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public class OrMatchPredicateImpl<V, I, O> implements OrMatchPredicate<V, I, O> {

    private final V valueObject;
    private final I valueAsInputType;
    private final O resultOutput;

    public OrMatchPredicateImpl(V valueObject,
                                I valueAsInputType,
                                O resultOutput) {
        this.valueObject = valueObject;
        this.valueAsInputType = valueAsInputType;
        this.resultOutput = resultOutput;
    }

    @Override
    public OrThenClause<V, I, O> and(Predicate<I> condition) {
        if (resultOutput != null) {
            return new OrThenClauseImpl<V, I, O>(valueObject,
                                                 null,
                                                 resultOutput);
        }
        if (valueAsInputType != null && Objects.nonNull(condition) && condition.test(valueAsInputType)) {
            return new OrThenClauseImpl<>(valueObject,
                                          valueAsInputType,
                                          null);
        }
        return new OrThenClauseImpl<>(valueObject,
                                      null,
                                      null);
    }

    @Override
    public OrMatchClause<V, I, O> then(Function<I, O> mapper) {
        if (resultOutput != null) {
            return new OrMatchClauseImpl<>(valueObject,
                                           resultOutput);
        }
        if (valueAsInputType != null && Objects.nonNull(mapper)) {
            return new OrMatchClauseImpl<>(valueObject,
                                           mapper.apply(valueAsInputType));
        }
        return new OrMatchClauseImpl<>(valueObject,
                                       null);
    }
}
