package io.github.anticipasean.girakkagraph.protocol.db;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Receive;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.supervisor.RegistrableProtocolSupervisor;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.BehaviorCreator;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SpawnedContext;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SubordinateSpawner;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.ActorRefRegistrations;
import io.github.anticipasean.girakkagraph.protocol.db.command.DatabaseProtocol;

public class DatabaseSupervisor extends RegistrableProtocolSupervisor<DatabaseProtocol> {

  protected DatabaseSupervisor(SpawnedContext<Command, DatabaseProtocol> spawnedContext) {
    super(spawnedContext);
    //    context.getLog().info("database supervisor initialized: [ " +
    // context.getSelf().path().toSerializationFormat() + ", " + entityManager + " ]");

  }

  public static Behavior<Command> create(ActorRef<Command> parentActorRef) {
    return BehaviorCreator.create(parentActorRef, DatabaseProtocol.class, DatabaseSupervisor::new);
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilderWithDefaultCommandHandlers().build();
  }

  @Override
  protected SubordinateSpawner<Command> spawnSubordinatesOnContextCreation() {
    return newSubordinateSpawner()
        .addSpawningBehavior(DatabaseProtocol.class, DatabaseProtocolActor::create);
  }

  @Override
  public ActorRefRegistrations<DatabaseProtocol> registrations() {
    return newRegistrationsBuilder().build();
  }
}
