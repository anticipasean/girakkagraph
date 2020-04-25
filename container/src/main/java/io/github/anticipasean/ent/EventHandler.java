package io.github.anticipasean.ent;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public interface EventHandler {

    public static <E> IfMatchTypeClause<E> onEvent(E eventObject) {
        return new IfMatchTypeClauseImpl<>(eventObject);
    }

    static <I, R> boolean isOfType(I inputObject,
                                   Class<R> returnType) {
        return inputObject != null && Objects.requireNonNull(returnType,
                                                             () -> "returnType specified may not be null")
                                             .isAssignableFrom(inputObject.getClass());
    }

    static <I, R> Optional<R> tryDynamicCast(I inputObject,
                                             Class<R> returnType) {
        try {
            return Optional.of(inputObject)
                           .map(input -> Objects.requireNonNull(returnType,
                                                                () -> "returnType specified may not be null for dynamic casting")
                                                .cast(input));
        } catch (ClassCastException e) {
            return Optional.empty();
        }
    }

    static interface IfMatchTypeClause<E> {

        <I> IfMatchPredicate<E, I> ifMatchesType(Class<I> possibleType);

    }

    static interface IfMatchPredicate<E, I> {

        ThenClause<E, I> and(Predicate<I> condition);
    }

    static interface IfNotMatchPredicate<E, I, O> {

        NextThenClause<E, I, O> and(Predicate<I> condition);
    }

    static interface ThenClause<E, I> {

        <O> IfNotMatchTypeClause<E, I, O> then(Function<I, O> func);

    }

    static interface IfNotMatchTypeClause<E, I, O> {

        <I> IfNotMatchPredicate<E, I, O> ifMatchesType(Class<I> possibleType);

        O orElse(O defaultOutput);

        <X extends Throwable> O orElseThrow(X throwable) throws X;

    }

    static interface NextThenClause<E, I, O> {

        IfNotMatchTypeClause<E, I, O> then(Function<I, O> func);
    }

    static interface MatchedThenClause<E, I, O> extends NextThenClause<E, I, O> {

    }

    static interface UnmatchedThenClause<E, I, O> extends NextThenClause<E, I, O> {

    }

    static class IfMatchTypeClauseImpl<E> implements IfMatchTypeClause<E> {

        private final E eventObject;

        public IfMatchTypeClauseImpl(E eventObject) {
            this.eventObject = eventObject;
        }

        @Override
        public <I> IfMatchPredicate<E, I> ifMatchesType(Class<I> possibleType) {
            if (isOfType(eventObject,
                         possibleType)) {
                return new IfMatchPredicateImpl<>(eventObject,
                                                  possibleType);
            }
            return new IfMatchPredicateImpl<>(eventObject,
                                              null);
        }
    }

    static class IfNotMatchPredicateImpl<E, I, O> implements IfNotMatchPredicate<E, I, O> {

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
            if (resultOutput != null) {
                return new MatchedThenClauseImpl<E, I, O>(eventObject,
                                                          null,
                                                          resultOutput);
            }
            if (matchedClass != null && Objects.nonNull(condition)) {
                Optional<I> eventAsPossibleTypeMaybe = tryDynamicCast(eventObject,
                                                                      matchedClass);
                if (eventAsPossibleTypeMaybe.isPresent()) {
                    return new MatchedThenClauseImpl<>(eventObject,
                                                       eventAsPossibleTypeMaybe.get(),
                                                       null);
                }
            }
            return new UnmatchedThenClauseImpl<>(eventObject);
        }
    }

    static class IfMatchPredicateImpl<E, I> implements IfMatchPredicate<E, I> {

        private final E eventObject;
        private final Class<I> matchedClass;

        public IfMatchPredicateImpl(E eventObject,
                                    Class<I> matchedClass) {
            this.eventObject = eventObject;
            this.matchedClass = matchedClass;
        }

        @Override
        public ThenClause<E, I> and(Predicate<I> condition) {
            if (matchedClass != null && Objects.nonNull(condition)) {
                Optional<I> eventAsPossibleTypeMaybe = tryDynamicCast(eventObject,
                                                                      matchedClass);
                if (eventAsPossibleTypeMaybe.isPresent()) {
                    return new ThenClauseImpl<>(eventObject,
                                                eventAsPossibleTypeMaybe.get());
                }
            }
            return new ThenClauseImpl<>(eventObject, null);
        }
    }

    static class ThenClauseImpl<E, I> implements ThenClause<E, I> {
        private final E eventObject;
        private final I matchedInput;

        public ThenClauseImpl(E eventObject,
                              I matchedInput) {
            this.eventObject = eventObject;
            this.matchedInput = matchedInput;
        }

        @Override
        public <O> IfNotMatchTypeClause<E, I, O> then(Function<I, O> func) {
            if (matchedInput != null) {
                O resultOutput = Objects.requireNonNull(func,
                                                        () -> "functionForInput may not be null")
                                        .apply(matchedInput);
                return new IfNotMatchTypeClauseImpl<E, I, O>(eventObject,
                                                             resultOutput);
            }
            return new IfNotMatchTypeClauseImpl<>(eventObject, null);
        }
    }

    static class MatchedThenClauseImpl<E, I, O> implements MatchedThenClause<E, I, O> {

        private final E eventObject;
        private final I matchedInput;
        private final O resultOutput;

        public MatchedThenClauseImpl(E eventObject,
                                     I matchedInput,
                                     O resultOutput) {
            this.eventObject = eventObject;
            this.matchedInput = matchedInput;
            this.resultOutput = resultOutput;
        }

        @Override
        public IfNotMatchTypeClause<E, I, O> then(Function<I, O> func) {
            if (resultOutput != null) {
                return new IfNotMatchTypeClauseImpl<E, I, O>(eventObject,
                                                             resultOutput);
            }
            if (matchedInput != null) {
                O resultOutput = Objects.requireNonNull(func,
                                                        () -> "functionForInput may not be null")
                                        .apply(matchedInput);
                return new IfNotMatchTypeClauseImpl<E, I, O>(eventObject,
                                                             resultOutput);
            }
            throw new IllegalStateException("MatchedThenClause should not be called without resultOutput or matchedInput");
        }
    }

    static class UnmatchedThenClauseImpl<E, I, O> implements UnmatchedThenClause<E, I, O> {

        private final E eventObject;

        public UnmatchedThenClauseImpl(E eventObject) {
            this.eventObject = eventObject;
        }

        @Override
        public IfNotMatchTypeClause<E, I, O> then(Function<I, O> func) {
            return new IfNotMatchTypeClauseImpl<>(eventObject,
                                                  null);
        }
    }

    static class IfNotMatchTypeClauseImpl<E, I, O> implements IfNotMatchTypeClause<E, I, O> {

        private final E eventObject;
        private final O resultOutput;

        public IfNotMatchTypeClauseImpl(E eventObject,
                                        O resultOutput) {
            this.eventObject = eventObject;
            this.resultOutput = resultOutput;
        }

        @Override
        public <I> IfNotMatchPredicate<E, I, O> ifMatchesType(Class<I> possibleType) {
            if (resultOutput != null) {
                return new IfNotMatchPredicateImpl<>(eventObject,
                                                     null,
                                                     resultOutput);
            } else if (isOfType(eventObject,
                                possibleType)) {
                return new IfNotMatchPredicateImpl<>(eventObject,
                                                     possibleType,
                                                     null);
            }
            return new IfNotMatchPredicateImpl<>(eventObject,
                                                 null,
                                                 null);
        }

        @Override
        public O orElse(O defaultOutput) {
            if (resultOutput != null) {
                return resultOutput;
            }
            return defaultOutput;
        }

        @Override
        public <X extends Throwable> O orElseThrow(X throwable) throws X {
            if (resultOutput != null) {
                return resultOutput;
            }
            throw throwable;
        }

    }
}


