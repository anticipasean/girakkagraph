package io.github.anticipasean.ent.func;

import cyclops.companion.Streamable;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import io.github.anticipasean.ent.iterator.TypeCheckingIterator;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface OrMatchClause1<V, I, O> extends Clause1<MatchResult<V, I, O>> {

    static <V, I, O> OrMatchClause1<V, I, O> of(Supplier<MatchResult<V, I, O>> supplier) {
        return new OrMatchClause1<V, I, O>() {
            @Override
            public MatchResult<V, I, O> get() {
                return supplier.get();
            }
        };
    }

    default <I> OrThenClause1<V, V, O> isEqualTo(I otherObject) {
        return OrThenClause1.of(() -> MatchResult.of(subject().either()
                                                              .mapLeft(tuple -> Option.of(tuple._1())
                                                                                      .filter(v -> v.equals(otherObject))
                                                                                      .fold(v -> Tuple2.of(v,
                                                                                                           Option.some(v)),
                                                                                            () -> Tuple2.of(tuple._1(),
                                                                                                            Option.none())))));
    }

    default <I> OrThenClause1<V, I, O> isOfType(Class<I> possibleType) {
        return OrThenClause1.of(() -> MatchResult.of(subject().either()
                                                              .mapLeft(tuple -> Option.of(tuple._1())
                                                                                      .flatMap(inputTypeMapper(possibleType))
                                                                                      .fold(i -> Tuple2.of(tuple._1(),
                                                                                                           Option.some(i)),
                                                                                            () -> Tuple2.of(tuple._1(),
                                                                                                            Option.none())))));
    }

    default <I> OrThenClause1<V, I, O> isOfTypeAnd(Class<I> possibleType,
                                                   Predicate<? super I> condition) {
        return OrThenClause1.of(() -> MatchResult.of(subject().either()
                                                              .mapLeft(tuple -> Option.of(tuple._1())
                                                                                      .flatMap(inputTypeMapper(possibleType))
                                                                                      .filter(condition)
                                                                                      .fold(i -> Tuple2.of(tuple._1(),
                                                                                                           Option.some(i)),
                                                                                            () -> Tuple2.of(tuple._1(),
                                                                                                            Option.none())))));
    }

    default OrThenClause1<V, V, O> fits(Predicate<? super V> condition) {
        return OrThenClause1.of(() -> MatchResult.of(subject().either()
                                                              .mapLeft(tuple -> tuple.first()
                                                                                     .filter(condition)
                                                                                     .fold(v -> Tuple2.of(v,
                                                                                                          Option.some(v)),
                                                                                           () -> Tuple2.of(subject().either()
                                                                                                                    .leftOrElse(null)
                                                                                                                    ._1(),
                                                                                                           Option.none())))));
    }

    default <T, E> OrThenIterableClause1<V, E, O> isIterableOver(Class<E> elementType) {
        return OrThenIterableClause1.of(() -> MatchResult.of(subject().either()
                                                                      .mapLeft(tuple -> Option.of(tuple._1())
                                                                                              .flatMap(inputTypeMapper(Iterable.class))
                                                                                              .map(iterable -> new TypeCheckingIterator<T, E>(iterable.iterator(),
                                                                                                                                              elementType))
                                                                                              .filter(TypeCheckingIterator::hasNext)
                                                                                              .map(iterator -> (Iterable<E>) (() -> iterator))
                                                                                              .fold(i -> Tuple2.of(tuple._1(),
                                                                                                                   Option.some(i)),
                                                                                                    () -> Tuple2.of(tuple._1(),
                                                                                                                    Option.none())))));
    }

    default <T, E> OrThenIterableClause1<V, E, O> isIterableOverAnd(Class<E> elementType,
                                                                    Predicate<Streamable<E>> condition) {
        return OrThenIterableClause1.of(() -> MatchResult.of(subject().either()
                                                                      .mapLeft(tuple -> Option.of(tuple._1())
                                                                                              .flatMap(inputTypeMapper(Iterable.class))
                                                                                              .map(iterable -> new TypeCheckingIterator<T, E>(iterable.iterator(),
                                                                                                                                              elementType))
                                                                                              .filter(TypeCheckingIterator::hasNext)
                                                                                              .map(iterator -> (Iterable<E>) (() -> iterator))
                                                                                              .map(Streamable::fromIterable)
                                                                                              .filter(condition)
                                                                                              .fold(i -> Tuple2.of(tuple._1(),
                                                                                                                   Option.some((Iterable<E>) i::iterator)),
                                                                                                    () -> Tuple2.of(tuple._1(),
                                                                                                                    Option.none())))));
    }

    default Option<O> yield() {
        return subject().either()
                        .fold(o -> Option.some(o),
                              () -> Option.none());
    }

    default O orElse(O defaultOutput) {
        return subject().either()
                        .fold(o -> o,
                              () -> defaultOutput);
    }

    default O orElseGet(Supplier<O> defaultOutputSupplier) {
        return subject().either()
                        .fold(o -> o,
                              defaultOutputSupplier);
    }

    default <X extends RuntimeException> O orElseThrow(Supplier<X> throwableSupplier) {
        if (subject().either()
                     .isRight()) {
            return subject().either()
                            .orElse(null);
        }
        throw throwableSupplier.get();
    }
}
