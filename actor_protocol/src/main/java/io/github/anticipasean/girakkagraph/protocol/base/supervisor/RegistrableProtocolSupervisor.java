package io.github.anticipasean.girakkagraph.protocol.base.supervisor;

import io.github.anticipasean.girakkagraph.protocol.base.actor.RegistrableCommandProtocolActor;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SpawnedContext;

public abstract class RegistrableProtocolSupervisor<T extends Command> extends
    RegistrableCommandProtocolActor<T>
    implements ProtocolSupervisor {

  protected RegistrableProtocolSupervisor(SpawnedContext<Command, T> spawnedContext) {
    super(spawnedContext);
  }
}
