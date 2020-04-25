package io.github.anticipasean.girakkagraph.protocol.base.util.spawn;

import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.Registrable;
import org.immutables.value.Value;

@Value.Immutable
public interface Spawnable<C, T> {

  Registrable<T> registrable();

  @Value.Derived
  default Class<T> protocolMessageType() {
    return registrable().protocolMessageType();
  }

  @Value.Derived
  default String id() {
    return registrable().id();
  }

  SpawnFromContextBehavior<C, T> spawnFromContextBehavior();
}
