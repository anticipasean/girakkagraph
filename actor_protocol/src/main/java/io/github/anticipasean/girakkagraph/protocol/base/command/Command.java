package io.github.anticipasean.girakkagraph.protocol.base.command;

import akka.actor.typed.ActorRef;
import io.github.anticipasean.girakkagraph.serialization.Jsonable;
import java.util.Optional;
import java.util.UUID;
import org.immutables.value.Value;

public interface Command<R> extends Jsonable {

  @Value.Default
  default UUID commandId() {
    return UUID.randomUUID();
  }

  Optional<ActorRef<R>> replyTo();

  Optional<Throwable> errorOccurred();
}
