package io.github.anticipasean.ent.node;

import com.oath.cyclops.types.Value;
import cyclops.function.Function0;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Node<V> extends Value<V> {

    static <V> Node<V> of(V value){
        return new Node<V>() {
            @Override
            public <R> R fold(Function<? super V, ? extends R> fn1,
                              Supplier<? extends R> s) {
                return fn1.apply(value);
            }

            @Override
            public Function0<V> asSupplier(V alt) {
                return () -> value;
            }

            @Override
            public boolean isPresent() {
                return true;
            }

            @Override
            public V orElse(V alt) {
                return value;
            }

            @Override
            public V orElseGet(Supplier<? extends V> s) {
                return value;
            }
        };
    }


}
