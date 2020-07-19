package io.github.anticipasean.ent.pattern;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class MatchPredicateImpl<E, I> implements MatchPredicate<E, I> {

    private final E eventObject;
    private final Class<I> matchedClass;

    public MatchPredicateImpl(E eventObject,
                              Class<I> matchedClass) {
        this.eventObject = eventObject;
        this.matchedClass = matchedClass;
    }

    @Override
    public ThenClause<E, I> and(Predicate<I> condition) {
        PatternMatching.logger.info("if_match_and predicate: state: obj {}, matchedClass {}",
                                    eventObject,
                                    matchedClass);
        if (matchedClass != null && Objects.nonNull(condition)) {
            Optional<I> eventAsPossibleTypeMaybe = PatternMatching.tryDynamicCast(eventObject,
                                                                                  matchedClass);
            if (eventAsPossibleTypeMaybe.isPresent() && condition.test(eventAsPossibleTypeMaybe.get())) {
                return new ThenClauseImpl<>(eventObject,
                                            eventAsPossibleTypeMaybe.get());
            }
        }
        return new ThenClauseImpl<>(eventObject,
                                    null);
    }

    @Override
    public <O> OrMatchClause<E, I, O> then(Function<I, O> func) {
        if (matchedClass != null) {
            I matchedInput = PatternMatching.tryDynamicCast(eventObject,
                                                            matchedClass).orElseThrow(() -> new RuntimeException(
                "unable to " + "case " + "object" + " to " + "matched " + "type"));
            O resultOutput = Objects.requireNonNull(func,
                                                    () -> "functionForInput may not be null")
                                    .apply(matchedInput);
            return new OrMatchClauseImpl<E, I, O>(eventObject,
                                                  resultOutput);
        }
        return new OrMatchClauseImpl<>(eventObject,
                                       null);
    }
}
