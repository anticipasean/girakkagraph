package io.github.anticipasean.girakkagraph.protocol.supervisor.command.monitoring;

import akka.actor.ActorPath;
import io.github.anticipasean.girakkagraph.protocol.supervisor.command.SupervisorProtocol;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
public interface SubordinateSupervisorTerminated
    extends SupervisorProtocol<SubordinateSupervisorTerminated> {
  Optional<Throwable> errorOccurred();

  Optional<ActorPath> actorPathOfTerminatedSupervisor();

  Optional<String> message();
}
