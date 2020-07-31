package io.github.anticipasean.ent.func;

import cyclops.function.Function1;

public interface Pattern1<V, O> extends Function1<Matcher<V>, O> {

    static <V, O> Function1<V, O> asMapper(Pattern1<V, O> pattern1){
        return pattern1.compose(Matcher::of);
    }

    @Override
    O apply(Matcher<V> matcher);
}
