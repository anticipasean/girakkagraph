package io.github.anticipasean.girakkagraph.protocol.db.service;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Receive;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.service.RegistrableActorRouterService;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.BehaviorCreator;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SpawnedContext;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.ActorRefRegistrations;
import io.github.anticipasean.girakkagraph.protocol.db.command.query.QueryDb;
import io.github.anticipasean.girakkagraph.protocol.db.worker.QueryDbWorker;
import java.util.Optional;

public class QueryDatabaseServiceActor extends RegistrableActorRouterService<QueryDb> {
  private final ActorRef<Command> routerRef;

  private QueryDatabaseServiceActor(SpawnedContext<Command, QueryDb> spawnedContext) {
    super(spawnedContext);
    String configPath = "akka.actor.deployment.database.query";
    routerRef = spawnRouter("QueryDb", QueryDbWorker::create, Optional.of(configPath));
  }

  public static Behavior<Command> create(ActorRef<Command> parentRef) {
    return BehaviorCreator.create(parentRef, QueryDb.class, QueryDatabaseServiceActor::new);
  }

  @Override
  public Receive<Command> createReceive() {
    return null;
  }

  @Override
  public ActorRefRegistrations<QueryDb> registrations() {
    return newRegistrationsBuilder().build();
  }
}
