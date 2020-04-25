package io.github.anticipasean.girakkagraph.protocol.base.worker;

import io.github.anticipasean.girakkagraph.protocol.base.actor.CommandProtocolActor;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SpawnedContext;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SubordinateSpawner;

public abstract class WorkerActor<T extends Command> extends CommandProtocolActor<T>
    implements Worker {

  protected WorkerActor(SpawnedContext<Command, T> spawnedContext) {
    super(spawnedContext);
    this.context().setLoggerName(loggingName());
  }

  protected String loggingName() {
    String actorPathLastSegment = selfRef().path().elements().last();
    if (actorPathLastSegment.length() > 0) {
      return String.join("_", role().actorNamingBasedOnRole().apply(registrable().id()), actorPathLastSegment);
    }
    return role().actorNamingBasedOnRole().apply(registrable().id());
  }

  @Override
  protected SubordinateSpawner<Command> spawnSubordinatesOnContextCreation() {
    return newSubordinateSpawner();
  }
}
