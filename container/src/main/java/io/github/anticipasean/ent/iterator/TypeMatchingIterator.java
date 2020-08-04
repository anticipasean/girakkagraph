package io.github.anticipasean.ent.iterator;

import cyclops.control.Option;
import io.github.anticipasean.ent.func.VariantMapper;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class TypeMatchingIterator<E> implements Iterator<E> {

    final Class<E> elementType;
    final Iterator<?> iteratorOfUnknownType;
    Option<E> next = Option.none();

    public TypeMatchingIterator(Iterator typeUnknownIterator,
                                Class<E> possibleElementType) {
        this.elementType = Objects.requireNonNull(possibleElementType,
                                                  "possibleElementType");
        this.iteratorOfUnknownType = Objects.requireNonNull(typeUnknownIterator,
                                                            "typeUnknownIterator");
    }

    @Override
    public boolean hasNext() {
        if (!next.isPresent() && iteratorOfUnknownType.hasNext()) {
            final Object candidate = iteratorOfUnknownType.next();
            // candidate should not be null based on the contract of hasNext for the Iterator interface
            // but we do not know what the backing iterator is and whether its type allows null entries
            next = Option.ofNullable(candidate)
                         .flatMap(VariantMapper.inputTypeMapper(elementType));
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TypeCheckingIterator{");
        sb.append("elementType=")
          .append(elementType);
        sb.append(", iteratorOfUnknownType=")
          .append(iteratorOfUnknownType);
        sb.append(", next=")
          .append(next);
        sb.append('}');
        return sb.toString();
    }
}
