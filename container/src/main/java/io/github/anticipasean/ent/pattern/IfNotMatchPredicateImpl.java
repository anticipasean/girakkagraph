package io.github.anticipasean.ent.pattern;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class IfNotMatchPredicateImpl<E, I, O> implements IfNotMatchPredicate<E, I, O> {

    private final E eventObject;
    private final Class<I> matchedClass;
    private final O resultOutput;

    public IfNotMatchPredicateImpl(E eventObject,
                                   Class<I> matchedClass,
                                   O resultOutput) {
        this.eventObject = eventObject;
        this.matchedClass = matchedClass;
        this.resultOutput = resultOutput;
    }

    @Override
    public NextThenClause<E, I, O> and(Predicate<I> condition) {
        Pattern.logger.info("if_not_match_and predicate: state: obj {}, matchedClass {}, resultObj {}",
                            eventObject,
                            matchedClass,
                            resultOutput);
        if (resultOutput != null) {
            return new MatchedThenClauseImpl<E, I, O>(eventObject,
                                                      null,
                                                      resultOutput);
        }
        if (matchedClass != null && Objects.nonNull(condition)) {
            Optional<I> eventAsPossibleTypeMaybe = Pattern.tryDynamicCast(eventObject,
                                                                          matchedClass);
            if (eventAsPossibleTypeMaybe.isPresent() && condition.test(eventAsPossibleTypeMaybe.get())) {
                return new MatchedThenClauseImpl<>(eventObject,
                                                   eventAsPossibleTypeMaybe.get(),
                                                   null);
            }
        }
        return new UnmatchedThenClauseImpl<>(eventObject);
    }

    @Override
    public IfNotMatchClause<E, I, O> then(Function<I, O> func) {
        if (resultOutput != null) {
            return new IfNotMatchClauseImpl<>(eventObject,
                                              resultOutput);
        }
        if (matchedClass != null) {
            Optional<I> matchedInput = Pattern.tryDynamicCast(eventObject,
                                                              matchedClass);
            if (matchedInput.isPresent()) {
                return new IfNotMatchClauseImpl<>(eventObject,
                                                  func.apply(matchedInput.get()));
            }
        }
        return new IfNotMatchClauseImpl<>(eventObject,
                                          null);
    }
}
