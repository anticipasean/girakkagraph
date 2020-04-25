package io.github.anticipasean.girakkagraph.protocol.db.worker;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.BehaviorCreator;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SpawnedContext;
import io.github.anticipasean.girakkagraph.protocol.base.worker.WorkerActor;
import io.github.anticipasean.girakkagraph.protocol.db.command.query.QueryDb;

public class QueryDbWorker extends WorkerActor<QueryDb> {

  private QueryDbWorker(SpawnedContext<Command, QueryDb> spawnedContext) {
    super(spawnedContext);
  }

  public static Behavior<Command> create(ActorRef<Command> parentActorRef) {
    return BehaviorCreator.create(parentActorRef, QueryDb.class, QueryDbWorker::new);
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilderWithDefaultCommandHandlers()
        .onMessage(QueryDb.class, this::onQueryDb)
        .build();
  }

  private Behavior<Command> onQueryDb(QueryDb cmd) {

    return Behaviors.same();
  }
}
