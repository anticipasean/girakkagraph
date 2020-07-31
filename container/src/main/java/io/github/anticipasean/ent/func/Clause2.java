package io.github.anticipasean.ent.func;

import com.oath.cyclops.matching.Deconstruct.Deconstruct2;
import cyclops.data.tuple.Tuple2;

public interface Clause2<K, V> extends Clause<Tuple2<K, V>>, Deconstruct2<K, V> {

    @Override
    default Tuple2<K, V> unapply(){
        return subject();
    }
}
