package io.github.anticipasean.girakkagraph.protocol.model.supervisor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.supervisor.RegistrableProtocolSupervisor;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.BehaviorCreator;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SpawnedContext;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.Subordinate;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SubordinateSpawner;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.ActorRefRegistrations;
import io.github.anticipasean.girakkagraph.protocol.model.ModelProtocolActor;
import io.github.anticipasean.girakkagraph.protocol.model.command.ModelProtocol;
import io.github.anticipasean.girakkagraph.protocol.model.command.ModelSupervisor;
import io.github.anticipasean.girakkagraph.protocol.model.command.modelindex.ModelIndexService;
import java.util.Optional;

public class ModelProtocolSupervisor extends RegistrableProtocolSupervisor<ModelSupervisor> {

  protected ModelProtocolSupervisor(SpawnedContext<Command, ModelSupervisor> spawnedContext) {
    super(spawnedContext);
  }

  public static Behavior<Command> create(ActorRef<Command> parentActorRef) {
    return BehaviorCreator.create(
        parentActorRef, ModelSupervisor.class, ModelProtocolSupervisor::new);
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
        .addSpawningBehavior(ModelProtocol.class, ModelProtocolActor::create);
  }

  @Override
  public ActorRefRegistrations<ModelSupervisor> registrations() {
    return newRegistrationsBuilder().build();
  }
}
