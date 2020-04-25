package io.github.anticipasean.ent.value;

import cyclops.control.Either;
import java.util.function.Function;

public interface Attribute<C> {

    static <C, V> Attribute<C> withContainer(C container,
                                             Function<C, V> valueExtractor) {

        return new Attribute<C>() {

            @Override
            public Either<Iterable<C>, C> container() {
                return Either.right(container);
            }

            @SuppressWarnings("unchecked")
            @Override
            public <V> Function<Either<Iterable<C>, C>, V> valueExtractor() {
                return either -> (V) either.map(valueExtractor)
                                           .orElse(null);
            }
        };
    }

    static <C, V> Attribute<C> withContainer(Iterable<C> container,
                                             Function<C, V> valueExtractor) {
        return new Attribute<C>() {
            @Override
            public Either<Iterable<C>, C> container() {
                return Either.left(container);
            }

            @SuppressWarnings("unchecked")
            @Override
            public <V> Function<Either<Iterable<C>, C>, V> valueExtractor() {
                return either -> (V) either.swap()
                                           .leftToStream()
                                           .map(valueExtractor)
                                           .singleOrElse(null);
            }
        };
    }

    Either<Iterable<C>, C> container();

    <V> Function<Either<Iterable<C>, C>, V> valueExtractor();

    //    protected static Comparator<Attribute<?>> attributeComparator() {
    //        return Comparator.<Attribute<?>, URI>comparing(Attribute::uri).thenComparing(Attribute::typeToken,
    //                                                                                     Comparator.comparing(Class::getSimpleName));
    //    }
    //    final Either<Iterable<C>, C> iterableOrScalar = Option.ofNullable(container)
    //                                                          .filterNot(val -> Iterable.class.isAssignableFrom(val.getClass()))
    //                                                          .map(Either::<Iterable<C>, C>right)
    //                                                          .orElseUse(Option.ofNullable(container)
    //                                                                           .map(Iterable.class::cast)
    //                                                                           .map(Either::<Iterable<C>, C>left))
    //                                                          .orElseGet(() -> Either.left(Collections.emptyList()));

}
