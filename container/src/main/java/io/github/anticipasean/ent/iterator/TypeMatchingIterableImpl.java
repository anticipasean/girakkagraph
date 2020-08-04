package io.github.anticipasean.ent.iterator;

import cyclops.data.LazySeq;
import cyclops.reactive.ReactiveSeq;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Supplier;

public class TypeMatchingIterableImpl<E> implements TypeMatchingIterable<E> {

    private final TypeMatchingIterator<E> typeMatchingIterator;
    private final ReactiveSeq<E> reactiveSeq;
    private final LazySeq<E> lazySeq;

    public TypeMatchingIterableImpl(TypeMatchingIterator<E> typeMatchingIterator) {
        this.typeMatchingIterator = typeMatchingIterator;
        this.reactiveSeq = ReactiveSeq.fromIterator(this.typeMatchingIterator);
        this.lazySeq = this.reactiveSeq.lazySeq();
    }

    private Supplier<Iterator<E>> iteratorSupplier() {
        return lazySeq::iterator;
    }

    @Override
    public Iterator<E> iterator() {
        return iteratorSupplier().get();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TypeMatchingIterableImpl{");
        sb.append("typeCheckingIterator=")
          .append(typeMatchingIterator);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TypeMatchingIterableImpl<?> that = (TypeMatchingIterableImpl<?>) o;
        return Objects.equals(typeMatchingIterator,
                              that.typeMatchingIterator) && Objects.equals(reactiveSeq,
                                                                           that.reactiveSeq) && Objects.equals(lazySeq,
                                                                                                               that.lazySeq);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeMatchingIterator,
                            reactiveSeq,
                            lazySeq);
    }
}
