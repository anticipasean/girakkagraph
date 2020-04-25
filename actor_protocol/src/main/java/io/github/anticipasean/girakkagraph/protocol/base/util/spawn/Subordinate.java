package io.github.anticipasean.girakkagraph.protocol.base.util.spawn;

import akka.actor.typed.ActorRef;
import org.immutables.value.Value;

@Value.Immutable
public interface Subordinate<C> {

  @Value.Default
  default Class<?> commandType() {
    return spawnable().protocolMessageType();
  }

  @Value.Default
  default String id() {
    return spawnable().id();
  }

  default SpawnFromContextBehavior<C, ?> spawnFromContextBehavior() {
    return spawnable().spawnFromContextBehavior();
  }

  Spawnable<C, ?> spawnable();

  ActorRef<C> subordinateRef();
}
