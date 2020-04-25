package io.github.anticipasean.girakkagraph.protocol.model;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.protocol.RegistrableProtocolActor;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.BehaviorCreator;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SpawnedContext;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.Subordinate;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SubordinateSpawner;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.ActorRefRegistrations;
import io.github.anticipasean.girakkagraph.protocol.model.command.ModelProtocol;
import io.github.anticipasean.girakkagraph.protocol.model.command.modelcriteria.ModelCriteriaService;
import io.github.anticipasean.girakkagraph.protocol.model.command.modelindex.ModelIndexService;
import io.github.anticipasean.girakkagraph.protocol.model.service.ModelCriteriaServiceActor;
import io.github.anticipasean.girakkagraph.protocol.model.service.ModelIndexServiceActor;
import java.util.Optional;

public class ModelProtocolActor extends RegistrableProtocolActor<ModelProtocol> {
  protected ModelProtocolActor(SpawnedContext<Command, ModelProtocol> spawnedContext) {
    super(spawnedContext);
  }

  public static Behavior<Command> create(ActorRef<Command> parentActorRef) {
    return BehaviorCreator.create(parentActorRef, ModelProtocol.class, ModelProtocolActor::new);
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilderWithDefaultCommandHandlers()
        .onMessage(ModelIndexService.class, this::onModelIndexCommand)
        .build();
  }

  private Behavior<Command> onModelIndexCommand(ModelIndexService modelIndexCommand) {
    context.getLog().info("model_index_command received: " + modelIndexCommand);
    Optional<Subordinate<Command>> subordinateMaybe =
        subordinateByCommandTypeHandled(ModelIndexService.class);
    subordinateMaybe.ifPresent(
        commandSubordinate -> commandSubordinate.subordinateRef().tell(modelIndexCommand));
    return Behaviors.same();
  }

  @Override
  protected SubordinateSpawner<Command> spawnSubordinatesOnContextCreation() {
    return newSubordinateSpawner()
        .addSpawningBehavior(ModelIndexService.class, ModelIndexServiceActor::create)
        .addSpawningBehavior(ModelCriteriaService.class, ModelCriteriaServiceActor::create);
  }

  @Override
  public ActorRefRegistrations<ModelProtocol> registrations() {
    return newRegistrationsBuilder().build();
  }
}
