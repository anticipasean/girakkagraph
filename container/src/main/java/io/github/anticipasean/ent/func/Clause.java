package io.github.anticipasean.ent.func;

import java.util.function.Supplier;

public interface Clause<S> extends Supplier<S> {

    default S subject() {
        return get();
    }

}
