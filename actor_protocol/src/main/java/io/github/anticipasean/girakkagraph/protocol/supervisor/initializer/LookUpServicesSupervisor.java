package io.github.anticipasean.girakkagraph.protocol.supervisor.initializer;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.DeathPactException;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.Receive;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.command.lookup.LookUpCommand;
import io.github.anticipasean.girakkagraph.protocol.base.command.lookup.shareable.LookUpShareables;
import io.github.anticipasean.girakkagraph.protocol.base.supervisor.RegistrableProtocolSupervisor;
import io.github.anticipasean.girakkagraph.protocol.base.util.monitoring.SupervisionPolicy;
import io.github.anticipasean.girakkagraph.protocol.base.util.monitoring.SupervisionPolicyImpl;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.BehaviorCreator;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SpawnFromParentRefBehavior;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SpawnedContext;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SubordinateSpawner;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.ActorRefRegistrations;
import io.github.anticipasean.girakkagraph.protocol.supervisor.service.lookup.LookUpShareableServiceActor;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import org.slf4j.event.Level;

public class LookUpServicesSupervisor extends RegistrableProtocolSupervisor<LookUpCommand> {

  protected LookUpServicesSupervisor(SpawnedContext<Command, LookUpCommand> spawnedContext) {
    super(spawnedContext);
  }

  private static List<SupervisionPolicy> supervisionPoliciesForLookUpSupervisor() {
    return Arrays.asList(
        SupervisionPolicyImpl.builder()
            .priority(9)
            .failureType(DeathPactException.class)
            .supervisorStrategy(SupervisorStrategy.restart().withLimit(3, Duration.ofMinutes(5)))
            .build(),
        SupervisionPolicyImpl.builder()
            .priority(10)
            .failureType(IllegalStateException.class)
            .supervisorStrategy(SupervisorStrategy.stop().withLogLevel(Level.ERROR))
            .build());
  }

  public static Behavior<Command> create(ActorRef<Command> parentRef) {
    return spawnFromParentRefBehaviorWithSupervisionPoliciesAdded().apply(parentRef);
  }

  private static SpawnFromParentRefBehavior<Command>
      spawnFromParentRefBehaviorWithSupervisionPoliciesAdded() {
    return BehaviorCreator.applySupervisionPoliciesToSpawnFromContextBehavior(
        parentRef ->
            BehaviorCreator.create(parentRef, LookUpCommand.class, LookUpServicesSupervisor::new),
        supervisionPoliciesForLookUpSupervisor());
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilderWithDefaultCommandHandlers().build();
  }

  @Override
  protected SubordinateSpawner<Command> spawnSubordinatesOnContextCreation() {
    return newSubordinateSpawner()
        .addSpawningBehavior(LookUpShareables.class, LookUpShareableServiceActor::create);
  }

  @Override
  public ActorRefRegistrations<LookUpCommand> registrations() {
    return newRegistrationsBuilder().build();
  }
}
