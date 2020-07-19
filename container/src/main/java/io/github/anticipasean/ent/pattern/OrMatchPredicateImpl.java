package io.github.anticipasean.ent.pattern;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class OrMatchPredicateImpl<E, I, O> implements OrMatchPredicate<E, I, O> {

    private final E eventObject;
    private final Class<I> matchedClass;
    private final O resultOutput;

    public OrMatchPredicateImpl(E eventObject,
                                Class<I> matchedClass,
                                O resultOutput) {
        this.eventObject = eventObject;
        this.matchedClass = matchedClass;
        this.resultOutput = resultOutput;
    }

    @Override
    public NextThenClause<E, I, O> and(Predicate<I> condition) {
        PatternMatching.logger.info("if_not_match_and predicate: state: obj {}, matchedClass {}, resultObj {}",
                                    eventObject,
                                    matchedClass,
                                    resultOutput);
        if (resultOutput != null) {
            return new NextThenClauseImpl<E, I, O>(eventObject,
                                                      null,
                                                      resultOutput);
        }
        if (matchedClass != null && Objects.nonNull(condition)) {
            Optional<I> eventAsPossibleTypeMaybe = PatternMatching.tryDynamicCast(eventObject,
                                                                                  matchedClass);
            if (eventAsPossibleTypeMaybe.isPresent() && condition.test(eventAsPossibleTypeMaybe.get())) {
                return new NextThenClauseImpl<>(eventObject,
                                                   eventAsPossibleTypeMaybe.get(),
                                                   null);
            }
        }
        return new NextThenClauseImpl<>(eventObject, null, null);
    }

    @Override
    public OrMatchClause<E, I, O> then(Function<I, O> func) {
        if (resultOutput != null) {
            return new OrMatchClauseImpl<>(eventObject,
                                           resultOutput);
        }
        if (matchedClass != null) {
            Optional<I> matchedInput = PatternMatching.tryDynamicCast(eventObject,
                                                                      matchedClass);
            if (matchedInput.isPresent()) {
                return new OrMatchClauseImpl<>(eventObject,
                                               func.apply(matchedInput.get()));
            }
        }
        return new OrMatchClauseImpl<>(eventObject,
                                       null);
    }
}
