package io.github.anticipasean.ent.iterator;

import cyclops.data.LazySeq;
import cyclops.function.Function0;
import cyclops.reactive.ReactiveSeq;
import java.util.Iterator;
import java.util.Objects;

class TypeMatchingIterableImpl<E> implements TypeMatchingIterable<E> {

    private final TypeMatchingIterator<E> typeMatchingIterator;
    private final ReactiveSeq<E> reactiveSeq;
    private final LazySeq<E> lazySeq;

    public TypeMatchingIterableImpl(TypeMatchingIterator<E> typeMatchingIterator) {
        this.typeMatchingIterator = typeMatchingIterator;
        this.reactiveSeq = ReactiveSeq.fromIterator(this.typeMatchingIterator);
        this.lazySeq = this.reactiveSeq.lazySeq();
    }

    /**
     * Use this iterator instead of the LazySeq one as it returns null when the iterator is out of elements instead of throwing
     * the API spec NoSuchElementException
     *
     * @param lazySeqParam
     * @param <E>
     * @return
     */
    private static <E> Function0<Iterator<E>> iteratorOverLazySeq(LazySeq<E> lazySeq) {
        return () -> new LazySeqIterator<>(lazySeq);
    }


    @Override
    public Iterator<E> iterator() {
        return iteratorOverLazySeq(lazySeq).apply();
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
