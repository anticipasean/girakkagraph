package io.github.anticipasean.girakkagraph.protocol.model.worker;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.util.dependencies.AkkaSpringDependencyInjectorExtensionId;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.BehaviorCreator;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SpawnedContext;
import io.github.anticipasean.girakkagraph.protocol.base.worker.WorkerActor;
import io.github.anticipasean.girakkagraph.protocol.model.command.modelcriteria.FetchCriteriaQuery;
import io.github.anticipasean.girakkagraph.protocol.model.command.modelcriteria.ModelCriteriaService;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;

public class ModelCriteriaWorker extends WorkerActor<ModelCriteriaService> {
  private AtomicReference<EntityManager> entityManagerSupplier;

  protected ModelCriteriaWorker(SpawnedContext<Command, ModelCriteriaService> spawnedContext) {
    super(spawnedContext);
    this.entityManagerSupplier = new AtomicReference<>();
  }

  public static Behavior<Command> create(ActorRef<Command> parentActorRef) {
    return BehaviorCreator.create(
        parentActorRef, ModelCriteriaService.class, ModelCriteriaWorker::new);
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilderWithDefaultCommandHandlers()
        .onMessage(FetchCriteriaQuery.class, this::onFetchCriteriaQuery)
        .build();
  }

  private Behavior<Command> onFetchCriteriaQuery(FetchCriteriaQuery fetchCriteriaQuery) {
    context.getLog().info("fetch_criteria_query received: " + fetchCriteriaQuery);
    if (!entityManagerAvailable()) {
      EntityManager entityManager = getEntityManagerFromAkkaExtension();
      if (entityManager == null) return Behaviors.stopped();
      this.entityManagerSupplier.set(entityManager);
    }
//    EntityManager entityManager = entityManagerSupplier.get();
//    QueryMapper criteriaQueryMapper = new QueryMapper(fetchCriteriaQuery, entityManager);
//    criteriaQueryMapper.buildCriteriaQuery();
    return Behaviors.stopped();
  }

  private boolean entityManagerAvailable() {
    return this.entityManagerSupplier.get() != null;
  }

  @Nullable
  private EntityManager getEntityManagerFromAkkaExtension() {
    EntityManager entityManager =
        AkkaSpringDependencyInjectorExtensionId.getInstance()
            .createExtension(context.getSystem())
            .getEntityManager();
    if (entityManager == null) {
      String message =
          "unable to obtain entity manager necessary for creating criteria query objects";
      context.getLog().error(message, new IllegalStateException(message));
      return null;
    }
    return entityManager;
  }
}
