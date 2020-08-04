package io.github.anticipasean.ent.iterator;

import cyclops.data.LazySeq;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

class LazySeqIterator<E> implements Iterator<E> {

    LazySeq<E> current;

    public LazySeqIterator(LazySeq<E> lazySeq) {
        current = lazySeq;
    }

    @Override
    public boolean hasNext() {
        return current.fold(c -> true,
                            n -> false);
    }

    @Override
    public E next() {
        return current.foldLazySeq(c -> {
                                       current = c.tail.get();
                                       return c.head.get();
                                   },
                                   n -> {
                                       throw new NoSuchElementException("no more elements available");
                                   });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LazySeqIterator<?> that = (LazySeqIterator<?>) o;
        return Objects.equals(current,
                              that.current);
    }

    @Override
    public int hashCode() {
        return Objects.hash(current);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LazyIterator{");
        sb.append("current=")
          .append(current);
        sb.append('}');
        return sb.toString();
    }
}
