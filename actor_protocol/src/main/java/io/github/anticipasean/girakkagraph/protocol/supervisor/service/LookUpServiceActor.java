package io.github.anticipasean.girakkagraph.protocol.supervisor.service;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.command.lookup.LookUpCommand;
import io.github.anticipasean.girakkagraph.protocol.base.command.lookup.shareable.LookUpShareables;
import io.github.anticipasean.girakkagraph.protocol.base.service.RegistrableActorRouterService;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.BehaviorCreator;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SpawnedContext;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.ActorRefRegistrations;

public class LookUpServiceActor extends RegistrableActorRouterService<LookUpCommand> {
  protected LookUpServiceActor(SpawnedContext<Command, LookUpCommand> spawnedContext) {
    super(spawnedContext);
  }

  public static Behavior<Command> create(ActorRef<Command> parentActorRef) {
    return BehaviorCreator.create(parentActorRef, LookUpCommand.class, LookUpServiceActor::new);
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilderWithDefaultCommandHandlers()
        .onMessage(LookUpShareables.class, this::onLookUpDependency)
        .build();
  }

  private Behavior<Command> onLookUpDependency(LookUpShareables lookUpShareables) {
    return Behaviors.same();
  }

  @Override
  public ActorRefRegistrations<LookUpCommand> registrations() {
    return newRegistrationsBuilder().build();
  }
}
