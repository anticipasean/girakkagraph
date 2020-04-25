package io.github.anticipasean.girakkagraph.protocol.base.util.container;

import akka.actor.typed.ActorRef;
import org.immutables.value.Value;

@Value.Immutable
public interface ActorRefValueContainer<T> extends ValueContainer<T, ActorRef, ActorRef<T>> {
  @Override
  Class<T> type();

  @Override
  @Value.Default
  default String name() {
    return new StringBuilder(containerType().getName())
        .append("<")
        .append(type().getName())
        .append(">")
        .toString();
  }

  @Override
  default Class<ActorRef> containerType() {
    return ActorRef.class;
  }

  @Override
  ActorRef<T> value();
}
