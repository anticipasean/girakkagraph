package io.github.anticipasean.ent.pattern.single;

import cyclops.companion.Streamable;
import cyclops.control.Either;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import io.github.anticipasean.ent.iterator.TypeMatchingIterable;
import io.github.anticipasean.ent.pattern.Clause;
import io.github.anticipasean.ent.pattern.VariantMapper;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface OrMatchClause1<V, I, O> extends Clause<MatchResult1<V, I, O>> {

    static <V, I, O> OrMatchClause1<V, I, O> of(Supplier<MatchResult1<V, I, O>> supplier) {
        return new OrMatchClause1<V, I, O>() {
            @Override
            public MatchResult1<V, I, O> get() {
                return supplier.get();
            }
        };
    }

    default <I> OrThenClause1<V, V, O> isEqualTo(I otherObject) {
        return OrThenClause1.of(() -> MatchResult1.of(subject().either()
                                                               .mapLeft(tuple -> Option.of(tuple._1())
                                                                                       .filter(v -> v.equals(otherObject))
                                                                                       .fold(v -> Tuple2.of(v,
                                                                                                            Option.some(v)),
                                                                                             () -> Tuple2.of(tuple._1(),
                                                                                                             Option.none())))));
    }

    default <I> OrThenClause1<V, I, O> isOfType(Class<I> possibleType) {
        return OrThenClause1.of(() -> MatchResult1.of(subject().either()
                                                               .mapLeft(tuple -> Option.of(tuple._1())
                                                                                       .flatMap(VariantMapper.inputTypeMapper(possibleType))
                                                                                       .fold(i -> Tuple2.of(tuple._1(),
                                                                                                            Option.some(i)),
                                                                                             () -> Tuple2.of(tuple._1(),
                                                                                                             Option.none())))));
    }

    default <I> OrThenClause1<V, I, O> isOfTypeAnd(Class<I> possibleType,
                                                   Predicate<? super I> condition) {
        return OrThenClause1.of(() -> MatchResult1.of(subject().either()
                                                               .mapLeft(tuple -> Option.of(tuple._1())
                                                                                       .flatMap(VariantMapper.inputTypeMapper(possibleType))
                                                                                       .filter(condition)
                                                                                       .fold(i -> Tuple2.of(tuple._1(),
                                                                                                            Option.some(i)),
                                                                                             () -> Tuple2.of(tuple._1(),
                                                                                                             Option.none())))));
    }

    default OrThenClause1<V, V, O> fits(Predicate<? super V> condition) {
        return OrThenClause1.of(() -> MatchResult1.of(subject().either()
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
        return OrThenIterableClause1.of(() -> MatchResult1.of(subject().either()
                                                                       .mapLeft(tuple -> Option.of(tuple._1())
                                                                                               .flatMap(VariantMapper.inputTypeMapper(Iterable.class))
                                                                                               .map(iterable -> TypeMatchingIterable.of(iterable.iterator(),
                                                                                                                                        elementType))
                                                                                               .filter(iterable -> iterable.iterator()
                                                                                                                           .hasNext())
                                                                                               .fold(i -> Tuple2.of(tuple._1(),
                                                                                                                    Option.some(i)),
                                                                                                     () -> Tuple2.of(tuple._1(),
                                                                                                                     Option.none())))));
    }

    default <T, E> OrThenIterableClause1<V, E, O> isIterableOverAnd(Class<E> elementType,
                                                                    Predicate<Streamable<E>> condition) {
        return OrThenIterableClause1.of(() -> MatchResult1.of(subject().either()
                                                                       .mapLeft(tuple -> Option.of(tuple._1())
                                                                                               .flatMap(VariantMapper.inputTypeMapper(Iterable.class))
                                                                                               .map(iterable -> TypeMatchingIterable.of(iterable.iterator(),
                                                                                                                                        elementType))
                                                                                               .filter(iterable -> iterable.iterator()
                                                                                                                           .hasNext())
                                                                                               .filter(iterable -> condition.test(Streamable.fromIterable(iterable)))
                                                                                               .fold(i -> Tuple2.of(tuple._1(),
                                                                                                                    Option.ofNullable(i)),
                                                                                                     () -> Tuple2.of(tuple._1(),
                                                                                                                     Option.none())))));
    }

    @SuppressWarnings("unchecked")
    default <I> OrThenOptionClause1<V, I, O> isOptionOfType(Class<I> inputType) {
        return OrThenOptionClause1.of(() -> MatchResult1.of(subject().either()
                                                                     .mapLeft(vIOptTuple -> Option.of(vIOptTuple._1())
                                                                                                  .flatMap(VariantMapper.inputTypeMapper(Option.class)))
                                                                     .mapLeft(optOpt -> optOpt.orElseGet(Option::<Option<Object>>none))
                                                                     .mapLeft(opt -> opt.orElse(null))
                                                                     .mapLeft(nullable -> Option.ofNullable(nullable)
                                                                                                .map(VariantMapper.inputTypeMapper(inputType)))
                                                                     .fold(opt -> Either.left(Tuple2.of(subject().unapply()
                                                                                                                 ._1(),
                                                                                                        opt.filter(Option::isPresent))),
                                                                           Either::right)));
    }

    @SuppressWarnings("unchecked")
    default <I> OrThenOptionClause1<V, I, O> isOptionOfTypeAnd(Class<I> inputType,
                                                               Predicate<I> condition) {
        return OrThenOptionClause1.of(() -> MatchResult1.of(subject().either()
                                                                     .mapLeft(vIOptTuple -> Option.of(vIOptTuple._1())
                                                                                                  .flatMap(VariantMapper.inputTypeMapper(Option.class)))
                                                                     .mapLeft(optOpt -> optOpt.orElseGet(Option::<Option<Object>>none))
                                                                     .mapLeft(opt -> opt.orElse(null))
                                                                     .mapLeft(nullable -> Option.ofNullable(nullable)
                                                                                                .map(VariantMapper.inputTypeMapper(inputType)))
                                                                     .fold(opt -> Either.left(Tuple2.of(subject().unapply()
                                                                                                                 ._1(),
                                                                                                        opt.filter(Option::isPresent)
                                                                                                           .filter(iOpt -> iOpt.filter(condition)
                                                                                                                               .isPresent()))),
                                                                           Either::right)));
    }

    default <I> OrThenClause1<V, I, O> mapsTo(Function<V, Option<I>> mapper) {
        return OrThenClause1.of(() -> MatchResult1.of(subject().either()
                                                               .mapLeft(tuple -> tuple.first()
                                                                                      .map(mapper)
                                                                                      ._1())
                                                               .mapLeft(iOpt -> Tuple2.of(subject().either()
                                                                                                   .leftOrElse(null)
                                                                                                   ._1(),
                                                                                          iOpt))));
    }

    default <I> OrThenClause1<V, I, O> mapsToAnd(Function<V, Option<I>> mapper,
                                                 Predicate<I> condition) {
        return OrThenClause1.of(() -> MatchResult1.of(subject().either()
                                                               .mapLeft(tuple -> tuple.first()
                                                                                      .map(mapper)
                                                                                      ._1()
                                                                                      .filter(condition))
                                                               .mapLeft(iOpt -> Tuple2.of(subject().either()
                                                                                                   .leftOrElse(null)
                                                                                                   ._1(),
                                                                                          iOpt))));
    }

    default O elseNullable() {
        return subject().either()
                        .orElse(null);
    }

    default Option<O> elseOption() {
        return subject().either()
                        .fold(Option::some,
                              Option::none);
    }


    default O elseDefault(O defaultOutput) {
        return subject().either()
                        .fold(o -> o,
                              () -> defaultOutput);
    }

    default O elseGet(Supplier<O> defaultOutputSupplier) {
        return subject().either()
                        .fold(o -> o,
                              defaultOutputSupplier);
    }

    default Either<V, O> elseOriginalOrResult() {
        return subject().either()
                        .mapLeft(Tuple2::_1);
    }

    default <X extends RuntimeException> O elseThrow(Function<V, X> throwableMapper) {
        if (subject().either()
                     .isRight()) {
            return subject().either()
                            .orElse(null);
        }
        throw throwableMapper.apply(subject().either()
                                             .leftOrElse(null)
                                             ._1());
    }
}
