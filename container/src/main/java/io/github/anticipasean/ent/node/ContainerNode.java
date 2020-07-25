package io.github.anticipasean.ent.node;

import cyclops.control.Reader;
import cyclops.function.Function0;
import java.util.function.Function;
import java.util.function.Supplier;

public interface ContainerNode<C, V> extends Node<C> {

    Reader<C, Iterable<V>> valueReader();

    static <C, V> Node<C> of(C container, Function<C, Iterable<V>> valueExtractor){
        return new ContainerNode<C, V>() {

            @Override
            public boolean isPresent() {
                return true;
            }

            @Override
            public Function0<C> asSupplier(C alt) {
                return () -> container;
            }

            @Override
            public C orElse(C alt) {
                return container;
            }

            @Override
            public C orElseGet(Supplier<? extends C> s) {
                return container;
            }

            @Override
            public <R> R fold(Function<? super C, ? extends R> fn1,
                              Supplier<? extends R> s) {
                return fn1.apply(container);
            }

            @Override
            public Reader<C, Iterable<V>> valueReader() {
                return Reader.of(valueExtractor);
            }
        };
    };

}
