package io.github.anticipasean.ent.func.single;

import cyclops.function.Function1;
import io.github.anticipasean.ent.func.Matcher;

public interface Pattern1<V, O> extends Function1<Matcher<V>, O> {

    static <V, O> Function1<V, O> asMapper(Pattern1<V, O> pattern1){
        return pattern1.compose(Matcher::of);
    }

    @Override
    O apply(Matcher<V> matcher);
}
