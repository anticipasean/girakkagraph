package io.github.anticipasean.ent.func;

import com.oath.cyclops.matching.Deconstruct.Deconstruct1;
import cyclops.data.tuple.Tuple1;

public interface Clause1<S> extends Clause<S>, Deconstruct1<S> {

    @Override
    default Tuple1<S> unapply() {
        return Tuple1.of(subject());
    }
}
