package io.github.anticipasean.girakkagraph.protocol.base.util.spawn;

import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.ActorContext;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.Registrable;
import org.immutables.value.Value;
import org.slf4j.Logger;

@Value.Immutable
public interface SpawnedContext<C, T> {

  static <C, T> SpawnedContext<C, T> newInstance(
      ActorContext<C> context, ActorRef<C> parentRef, Spawnable<C, T> spawnable) {
    return SpawnedContextImpl.<C, T>builder()
        .context(context)
        .parentRef(parentRef)
        .spawnable(spawnable)
        .build();
  }

  ActorContext<C> context();

  ActorRef<C> parentRef();

  @Value.Derived
  default ActorRef<C> selfRef() {
    return context().getSelf();
  }

  @Value.Derived
  default Logger logger() {
    return context().getLog();
  }

  @Value.Derived
  default Registrable<T> registrable() {
    return spawnable().registrable();
  }

  @Value.Derived
  default Class<T> protocolMessageType() {
    return registrable().protocolMessageType();
  }

  default String id(){return registrable().id();}

  @Value.Derived
  default SpawnFromContextBehavior<C, T> spawnFromContextBehavior() {
    return spawnable().spawnFromContextBehavior();
  }

  Spawnable<C, T> spawnable();
}
