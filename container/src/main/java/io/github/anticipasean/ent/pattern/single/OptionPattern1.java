package io.github.anticipasean.ent.pattern.single;

import cyclops.control.Option;
import cyclops.function.Function1;
import io.github.anticipasean.ent.pattern.Matcher;
import io.github.anticipasean.ent.pattern.Matcher.Matcher1;

public interface OptionPattern1<V, O> extends Pattern1<Matcher1<V>, Option<O>> {

    static <V, O> Function1<V, O> asMapper(Pattern1<V, O> pattern1) {
        return pattern1.compose(Matcher::of);
    }

    @Override
    Option<O> apply(Matcher1<Matcher1<V>> matcher);
}
