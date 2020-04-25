package io.github.anticipasean.girakkagraph.protocol.db;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.protocol.RegistrableProtocolActor;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.BehaviorCreator;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SpawnedContext;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SubordinateSpawner;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.ActorRefRegistrations;
import io.github.anticipasean.girakkagraph.protocol.db.command.DatabaseProtocol;
import io.github.anticipasean.girakkagraph.protocol.db.command.query.QueryDb;
import io.github.anticipasean.girakkagraph.protocol.db.service.QueryDatabaseServiceActor;

public class DatabaseProtocolActor extends RegistrableProtocolActor<DatabaseProtocol> {

  private DatabaseProtocolActor(SpawnedContext<Command, DatabaseProtocol> spawnedContext) {
    super(spawnedContext);
  }

  public static Behavior<Command> create(ActorRef<Command> parentActorRef) {
    return BehaviorCreator.create(
        parentActorRef, DatabaseProtocol.class, DatabaseProtocolActor::new);
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilderWithDefaultCommandHandlers()
        //        .onMessage(
        //            GetDatabaseServiceKeyMap.class,
        //            cmd -> {
        //              return super.onGetServiceKeyMap(cmd, topLevelCmdToServiceKeyMap);
        //            })
        .onMessage(QueryDb.class, this::onQueryDb)
        .build();
  }

  private Behavior<Command> onQueryDb(QueryDb cmd) {

    // TODO: Add protocol handler for queries here
    return Behaviors.same();
  }

  @Override
  protected SubordinateSpawner<Command> spawnSubordinatesOnContextCreation() {
    return newSubordinateSpawner()
        .addSpawningBehavior(QueryDb.class, QueryDatabaseServiceActor::create);
  }

  @Override
  public ActorRefRegistrations<DatabaseProtocol> registrations() {
    return newRegistrationsBuilder().build();
  }
}
