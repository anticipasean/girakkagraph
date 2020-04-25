package io.github.anticipasean.girakkagraph.protocol.base.service;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import io.github.anticipasean.girakkagraph.protocol.base.actor.RegistrableCommandProtocolActor;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.util.routing.RouterConfigurable;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SpawnedContext;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SubordinateSpawner;
import java.util.Optional;
import java.util.function.Function;

public abstract class RegistrableActorRouterService<T extends Command> extends
    RegistrableCommandProtocolActor<T>
    implements RouterConfigurable<Command>, ActorRouterService {

  protected RegistrableActorRouterService(SpawnedContext<Command, T> spawnedContext) {
    super(spawnedContext);
  }

  protected ActorRef<Command> spawnRouter(
      String routerName,
      Function<ActorRef<Command>, Behavior<Command>> createWorkerBehaviorWithParentServiceRef,
      Optional<String> pathToRouterConfig) {
    ActorRef<Command> routerRef =
        this.spawnRouter(
            context, routerName, createWorkerBehaviorWithParentServiceRef, pathToRouterConfig);
    return routerRef;
  }

  @Override
  protected SubordinateSpawner<Command> spawnSubordinatesOnContextCreation() {
    return newSubordinateSpawner();
  }
}
