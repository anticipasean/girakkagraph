package io.github.anticipasean.girakkagraph.protocol.base.command.status;

import akka.NotUsed;
import akka.actor.ActorPath;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import org.immutables.value.Value;

@Value.Immutable
public interface Initialized extends Command<NotUsed> {
  ActorPath actorPath();
}
