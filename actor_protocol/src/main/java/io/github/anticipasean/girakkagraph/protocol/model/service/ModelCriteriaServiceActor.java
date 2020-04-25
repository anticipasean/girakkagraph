package io.github.anticipasean.girakkagraph.protocol.model.service;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.service.RegistrableActorRouterService;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.BehaviorCreator;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SpawnedContext;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.ActorRefRegistrations;
import io.github.anticipasean.girakkagraph.protocol.model.command.modelcriteria.FetchCriteriaQuery;
import io.github.anticipasean.girakkagraph.protocol.model.command.modelcriteria.ModelCriteriaService;
import io.github.anticipasean.girakkagraph.protocol.model.worker.ModelCriteriaWorker;
import java.util.Optional;

public class ModelCriteriaServiceActor extends RegistrableActorRouterService<ModelCriteriaService> {
  private final ActorRef<Command> routerRef;

  protected ModelCriteriaServiceActor(
      SpawnedContext<Command, ModelCriteriaService> spawnedContext) {
    super(spawnedContext);
    this.routerRef = spawnRouter("ModelCriteria", ModelCriteriaWorker::create, Optional.empty());
  }

  public static Behavior<Command> create(ActorRef<Command> parentActorRef) {
    return BehaviorCreator.create(
        parentActorRef, ModelCriteriaService.class, ModelCriteriaServiceActor::new);
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilderWithDefaultCommandHandlers()
        .onMessage(FetchCriteriaQuery.class, this::onFetchCriteriaQuery)
        .build();
  }

  private Behavior<Command> onFetchCriteriaQuery(FetchCriteriaQuery fetchCriteriaQuery) {
    context.getLog().info("fetch_criteria_query received: " + fetchCriteriaQuery);
    this.routerRef.tell(fetchCriteriaQuery);
    return Behaviors.same();
  }

  @Override
  public ActorRefRegistrations<ModelCriteriaService> registrations() {
    return newRegistrationsBuilder().build();
  }
}
