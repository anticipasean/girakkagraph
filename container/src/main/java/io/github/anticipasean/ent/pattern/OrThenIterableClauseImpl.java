package io.github.anticipasean.ent.pattern;

import cyclops.companion.Streamable;
import java.util.function.Function;

public class OrThenIterableClauseImpl<V, I, O> implements OrThenIterableClause<V, I, O> {
    private final V valueObject;
    private final Iterable<I> inputIterable;
    private final O resultObject;

    public OrThenIterableClauseImpl(V valueObject,
                                    Iterable<I> inputIterable,
                                    O resultObject) {
        this.valueObject = valueObject;
        this.inputIterable = inputIterable;
        this.resultObject = resultObject;
    }

    @Override
    public OrMatchClause<V, I, O> then(Function<Streamable<I>, O> mapper) {
        if(resultObject != null){
            return new OrMatchClauseImpl<>(valueObject, resultObject);
        } else if (inputIterable != null && inputIterable.iterator().hasNext()){
            return new OrMatchClauseImpl<>(valueObject, mapper.apply(Streamable.fromIterable(inputIterable)));
        } else {
            return new OrMatchClauseImpl<>(valueObject, null);
        }
    }
}
