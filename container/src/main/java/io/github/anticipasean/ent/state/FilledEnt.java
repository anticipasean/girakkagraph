package io.github.anticipasean.ent.state;

import cyclops.data.ImmutableMap;
import io.github.anticipasean.ent.Ent;

public class FilledEnt<K, V> implements Ent<K, V> {

    private final ImmutableMap<K, V> data;

    public FilledEnt(ImmutableMap<K, V> data) {
        this.data = data;
    }

    @Override
    public ImmutableMap<K, V> toImmutableMap() {
        return data;
    }

}
