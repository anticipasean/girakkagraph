package io.github.anticipasean.girakkagraph.protocol.base.command.monitoring;

import akka.NotUsed;
import akka.actor.ActorPath;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
public interface SubordinateTerminated extends Command<NotUsed> {

  Optional<String> id();

  Optional<Class<?>> registeredCommandType();

  ActorPath actorPathOfTerminatedSubordinate();

  Optional<String> message();
}
