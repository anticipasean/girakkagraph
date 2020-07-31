package io.github.anticipasean.ent.func;

import cyclops.function.Function0;

public interface Clause<S> extends Function0<S>, Divergent {

    default S subject() {
        return get();
    }

}
