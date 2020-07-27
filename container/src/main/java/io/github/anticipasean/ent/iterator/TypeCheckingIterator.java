package io.github.anticipasean.ent.iterator;

import cyclops.control.Option;
import io.github.anticipasean.ent.pattern.PatternMatching;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class TypeCheckingIterator<T, E> implements Iterator<E> {

    final Class<E> elementType;
    final Iterator<T> iteratorOfUnknownType;
    Option<E> next = Option.none();

    @SuppressWarnings("unchecked")
    public TypeCheckingIterator(Iterator typeUnknownIterator,
                                Class<E> possibleElementType) {
        this.elementType = Objects.requireNonNull(possibleElementType,
                                                  "possibleElementType");
        this.iteratorOfUnknownType = Objects.requireNonNull(typeUnknownIterator,
                                                            "typeUnknownIterator");
    }

    @Override
    public boolean hasNext() {
        while (!next.isPresent() && iteratorOfUnknownType.hasNext()) {
            final T candidate = iteratorOfUnknownType.next();
            if (PatternMatching.isOfType(candidate,
                                         elementType)) {
                next = PatternMatching.tryDynamicCast(candidate,
                                                      elementType);
            }
        }
        return next.isPresent();
    }

    @Override
    public E next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        final E result = next.orElse(null);
        next = Option.none();
        return result;
    }

}
