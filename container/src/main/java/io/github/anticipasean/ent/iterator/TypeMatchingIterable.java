package io.github.anticipasean.ent.iterator;

import java.util.Iterator;

public interface TypeMatchingIterable<E> extends Iterable<E> {

    static <E> Iterable<E> of(TypeMatchingIterator<E> typeMatchingIterator) {
        return new TypeMatchingIterableImpl<E>(typeMatchingIterator);
    }

    static <E> Iterable<E> of(Iterator iteratorOfUnknownType,
                              Class<E> elementType) {
        return new TypeMatchingIterableImpl<E>(new TypeMatchingIterator<>(iteratorOfUnknownType,
                                                                          elementType));
    }

}
