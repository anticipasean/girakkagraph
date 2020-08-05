package io.github.anticipasean.ent.pattern;

import java.util.function.Supplier;

public interface Clause<S> extends Supplier<S> {

    default S subject() {
        return get();
    }

}
