package io.github.anticipasean.girakkagraph.protocol.supervisor;

import akka.actor.ActorPath;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.command.monitoring.SubordinateTerminated;
import io.github.anticipasean.girakkagraph.protocol.base.command.status.Initialized;
import io.github.anticipasean.girakkagraph.protocol.base.command.status.InitializedImpl;
import io.github.anticipasean.girakkagraph.protocol.base.command.status.StartUp;
import io.github.anticipasean.girakkagraph.protocol.base.supervisor.RegistrableProtocolSupervisor;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.Spawnable;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SpawnableImpl;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SpawnedContext;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.Subordinate;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SubordinateSpawner;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.ActorRefRegistrations;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.ActorRefRegistrationsImpl;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.RegistrableImpl;
import io.github.anticipasean.girakkagraph.protocol.supervisor.command.SupervisorProtocol;
import io.github.anticipasean.girakkagraph.protocol.supervisor.initializer.TopSupervisor;
import java.util.Optional;

public class SupervisorProtocolActor extends RegistrableProtocolSupervisor<Command> {
  private ActorRef<Command> topSupervisor;
  private Optional<ActorRef<Initialized>> startUpSystemCaller;

  protected SupervisorProtocolActor(SpawnedContext<Command, Command> spawnedContext) {
    super(spawnedContext);
    this.topSupervisor = retrieveCreatedTopSupervisorActorRef();
    //    createSpringDependencyInjectorExtensionForActorSystemUse();
  }

  /**
   * Special handling required for the top node since it lacks a parent
   *
   * @return the supervisor protocol actor as a behavior
   */
  public static Behavior<Command> create() {
    return Behaviors.setup(
        actorContext -> {
          SpawnedContext<Command, Command> spawnedContext =
              //Need to use custom class here because the standard SpawnedContext builder will
              //check for null values including one for the parent ref
              //and the SupervisorProtocolActor does not have a parent ref
              new SpawnedContext<Command, Command>() {
                @Override
                public ActorContext<Command> context() {
                  return actorContext;
                }

                @Override
                public ActorRef<Command> parentRef() {
                  return null;
                }

                @Override
                public Spawnable<Command, Command> spawnable() {
                  return SpawnableImpl.<Command, Command>builder()
                      .registrable(RegistrableImpl.of(Command.class))
                      .spawnFromContextBehavior(SupervisorProtocolActor::new)
                      .build();
                }
              };
          return new SupervisorProtocolActor(spawnedContext);
        });
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilderWithDefaultCommandHandlers()
        .onMessage(StartUp.class, this::onStartUp)
        .build();
  }

  @Override
  public ActorRef<Command> parentRef() {
    throw new UnsupportedOperationException(
        String.format(
            "may not call for parent ref on %s. it does not have a parent",
            SupervisorProtocolActor.class.getName()));
  }

  private ActorRef<Command> retrieveCreatedTopSupervisorActorRef() {
    ActorRef<Command> topSupervisorRef =
        subordinateByCommandTypeHandled(SupervisorProtocol.class)
            .map(Subordinate::subordinateRef)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "unable to spawn top supervisor and retrieve actor ref"));
    context.getLog().info("top supervisor spawned: " + topSupervisorRef);
    return topSupervisorRef;
  }

  private void createSpringDependencyInjectorExtensionForActorSystemUse() {
    context
        .getLog()
        .info(
            "instantiate dependency holder for actor system;"
                + " must be created in actor system context once before use");
    //    AkkaSpringDependencyInjectorExtensionId extensionId =
    //        AkkaSpringDependencyInjectorExtensionId.getInstance();
    //    AkkaSpringDependencyInjector springDependencyInjector =
    //        extensionId.createExtension(context.getSystem());
  }

  private Behavior<Command> onStartUp(StartUp command) {
    context.getLog().info("start_up received: " + command);
    replyToIfPresent(
        command,
        InitializedImpl.builder()
            .actorPath(selfRef().path())
            .commandId(command.commandId())
            .build());
    return Behaviors.same();
  }

  @Override
  protected SubordinateSpawner<Command> spawnSubordinatesOnContextCreation() {
    return newSubordinateSpawner()
        .addSpawningBehavior(SupervisorProtocol.class, TopSupervisor::create);
  }

  @Override
  protected Behavior<Command> onSubordinateTerminated(SubordinateTerminated command) {
    context.getLog().info(String.format("subordinate_supervisor_terminated: %s", command));
    ActorPath actorPathOfTerminated = command.actorPathOfTerminatedSubordinate();
    if (actorPathOfTerminated.equals(topSupervisor.path())) {
      context.getSystem().terminate();
    }
    return Behaviors.unhandled();
  }

  @Override
  public ActorRefRegistrations<Command> registrations() {
    return ActorRefRegistrationsImpl.<Command>builder().build();
  }
}
