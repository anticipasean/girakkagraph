package io.github.anticipasean.ent.func;

import cyclops.companion.Streamable;
import cyclops.control.Either;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple1;
import cyclops.data.tuple.Tuple2;
import io.github.anticipasean.ent.iterator.TypeCheckingIterator;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Matcher {

    static <V> Function<V, MatchClause<V>> starter() {
        return v -> patternForValue(v);
    }

    static <V> MatchClause<V> patternForValue(V value) {
        return MatchClause.of(() -> value);
    }

    static interface MatchResult<V, I, O> {

        static <V, I, O> MatchResult<V, I, O> of(Either<Tuple2<V, Option<I>>, O> valueInputTupleOrResultObject) {
            return new MatchResult<V, I, O>() {
                @Override
                public Either<Tuple2<V, Option<I>>, O> either() {
                    return valueInputTupleOrResultObject;
                }
            };
        }

        Either<Tuple2<V, Option<I>>, O> either();

    }

    static interface MatchClause<V> extends Supplier<V> {

        static <V> MatchClause<V> of(Supplier<V> valueSupplier) {
            return new MatchClause<V>() {
                @Override
                public V get() {
                    return valueSupplier.get();
                }
            };
        }

        default <I> ThenClause<V, I> ifOfType(Class<I> possibleType) {
            return ThenClause.of(() -> Tuple1.of(get())
                                             .ofType(possibleType)
                                             .fold(i -> Tuple2.of(get(),
                                                                  Option.some(i)),
                                                   () -> Tuple2.of(get(),
                                                                   Option.none())));
        }

        default <T, E> ThenIterableClause<V, E> ifIterableOver(Class<E> elementType) {
            return ThenIterableClause.of(() -> Tuple1.of(get())
                                                     .ofType(Iterable.class)
                                                     .map(iterable -> new TypeCheckingIterator<T, E>(iterable.iterator(),
                                                                                                     elementType))
                                                     .filter(TypeCheckingIterator::hasNext)
                                                     .map(iter -> (Iterable<E>) (() -> iter))
                                                     .fold(iterable -> Tuple2.of(get(),
                                                                                 Option.some(iterable)),
                                                           () -> Tuple2.of(get(),
                                                                           Option.none())));
        }


    }

    static interface ThenClause<V, I> extends Supplier<Tuple2<V, Option<I>>> {

        static <V, I, O> ThenClause<V, I> of(Supplier<Tuple2<V, Option<I>>> valueSupplier) {
            return new ThenClause<V, I>() {
                @Override
                public Tuple2<V, Option<I>> get() {
                    return valueSupplier.get();
                }
            };
        }

        default <O> OrMatchClause<V, I, O> then(Function<I, O> mapper) {
            return OrMatchClause.of(() -> MatchResult.of(get().map2(inputTypeAsOpt -> inputTypeAsOpt.map(mapper)
                                                                                                    .toEither(Tuple2.of(get()._1(),
                                                                                                                        Option.<I>none())))
                                                              ._2()));
        }

    }

    static interface ThenIterableClause<V, I> extends Supplier<Tuple2<V, Option<Iterable<I>>>> {

        static <V, I, O> ThenIterableClause<V, I> of(Supplier<Tuple2<V, Option<Iterable<I>>>> valueSupplier) {
            return new ThenIterableClause<V, I>() {
                @Override
                public Tuple2<V, Option<Iterable<I>>> get() {
                    return valueSupplier.get();
                }
            };
        }

        default <O> OrMatchClause<V, I, O> then(Function<Streamable<I>, O> mapper) {
            return OrMatchClause.of(() -> MatchResult.of(get().map2(inputTypeAsOpt -> inputTypeAsOpt.map(Streamable::fromIterable)
                                                                                                    .map(mapper)
                                                                                                    .toEither(Tuple2.of(get()._1(),
                                                                                                                        Option.<I>none())))
                                                              ._2()));
        }

    }

    static interface OrMatchClause<V, I, O> extends Supplier<MatchResult<V, I, O>> {

        static <V, I, O> OrMatchClause<V, I, O> of(Supplier<MatchResult<V, I, O>> supplier) {
            return new OrMatchClause<V, I, O>() {
                @Override
                public MatchResult<V, I, O> get() {
                    return supplier.get();
                }
            };
        }

        default <I> OrThenClause<V, I, O> ifOfType(Class<I> possibleType) {
            return OrThenClause.of(() -> MatchResult.of(get().either()
                                                             .mapLeft(tuple -> Tuple1.of(tuple._1())
                                                                                     .ofType(possibleType)
                                                                                     .fold(i -> Tuple2.of(tuple._1(),
                                                                                                          Option.some(i)),
                                                                                           () -> Tuple2.of(tuple._1(),
                                                                                                           Option.none())))));
        }

        default <T, E> OrThenIterableClause<V, E, O> ifIterableOver(Class<E> elementType) {
            return OrThenIterableClause.of(() -> MatchResult.of(get().either()
                                                                     .mapLeft(tuple -> Tuple1.of(tuple._1())
                                                                                             .ofType(Iterable.class)
                                                                                             .map(iterable -> new TypeCheckingIterator<T, E>(iterable.iterator(),
                                                                                                                                             elementType))
                                                                                             .filter(TypeCheckingIterator::hasNext)
                                                                                             .map(iterator -> (Iterable<E>) (() -> iterator))
                                                                                             .fold(i -> Tuple2.of(tuple._1(),
                                                                                                                  Option.some(i)),
                                                                                                   () -> Tuple2.of(tuple._1(),
                                                                                                                   Option.none())))));
        }

        default Option<O> asOption() {
            return get().either()
                        .fold(o -> Option.some(o),
                              () -> Option.none());
        }

        default O orElse(O defaultOutput) {
            return get().either()
                        .fold(o -> o,
                              () -> defaultOutput);
        }

    }

    static interface OrThenClause<V, I, O> extends Supplier<MatchResult<V, I, O>> {

        static <V, I, O> OrThenClause<V, I, O> of(Supplier<MatchResult<V, I, O>> supplier) {
            return new OrThenClause<V, I, O>() {
                @Override
                public MatchResult<V, I, O> get() {
                    return supplier.get();
                }
            };
        }

        default OrMatchClause<V, I, O> then(Function<I, O> mapper) {
            return OrMatchClause.of(() -> MatchResult.of(get().either()
                                                              .mapLeft(Tuple2::_2)
                                                              .mapLeft(valueAsInputTypeOpt -> valueAsInputTypeOpt.map(mapper))
                                                              .flatMapLeft(outputTypeOpt -> outputTypeOpt.toEither(Tuple2.of(get().either()
                                                                                                                                  .leftOrElse(null)
                                                                                                                                  ._1(),
                                                                                                                             Option.none())))));
        }

    }

    static interface OrThenIterableClause<V, I, O> extends Supplier<MatchResult<V, Iterable<I>, O>> {

        static <V, I, O> OrThenIterableClause<V, I, O> of(Supplier<MatchResult<V, Iterable<I>, O>> supplier) {
            return new OrThenIterableClause<V, I, O>() {
                @Override
                public MatchResult<V, Iterable<I>, O> get() {
                    return supplier.get();
                }
            };
        }

        default OrMatchClause<V, I, O> then(Function<Streamable<I>, O> mapper) {
            return OrMatchClause.of(() -> MatchResult.of(get().either()
                                                              .mapLeft(Tuple2::_2)
                                                              .mapLeft(valueAsInputTypeOpt -> valueAsInputTypeOpt.map(Streamable::fromIterable)
                                                                                                                 .map(mapper))
                                                              .flatMapLeft(outputTypeOpt -> outputTypeOpt.toEither(Tuple2.of(get().either()
                                                                                                                                  .leftOrElse(null)
                                                                                                                                  ._1(),
                                                                                                                             Option.none())))));
        }

    }

}
