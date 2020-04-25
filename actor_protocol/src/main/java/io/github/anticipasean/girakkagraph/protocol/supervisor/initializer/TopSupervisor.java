package io.github.anticipasean.girakkagraph.protocol.supervisor.initializer;

import akka.actor.ActorPath;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.DeathPactException;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.command.lookup.LookUpCommand;
import io.github.anticipasean.girakkagraph.protocol.base.command.monitoring.SubordinateTerminated;
import io.github.anticipasean.girakkagraph.protocol.base.supervisor.RegistrableProtocolSupervisor;
import io.github.anticipasean.girakkagraph.protocol.base.util.monitoring.SupervisionPolicy;
import io.github.anticipasean.girakkagraph.protocol.base.util.monitoring.SupervisionPolicyImpl;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.BehaviorCreator;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SpawnFromParentRefBehavior;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SpawnedContext;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SubordinateSpawner;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.ActorRefRegistrations;
import io.github.anticipasean.girakkagraph.protocol.graphql.GraphQLSupervisor;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.GraphQlProtocol;
import io.github.anticipasean.girakkagraph.protocol.model.command.ModelProtocol;
import io.github.anticipasean.girakkagraph.protocol.model.supervisor.ModelProtocolSupervisor;
import io.github.anticipasean.girakkagraph.protocol.supervisor.command.SupervisorProtocol;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import org.slf4j.event.Level;

public class TopSupervisor extends RegistrableProtocolSupervisor<SupervisorProtocol> {

  protected TopSupervisor(SpawnedContext<Command, SupervisorProtocol> spawnedContext) {
    super(spawnedContext);
  }

  private static List<SupervisionPolicy> supervisionPoliciesForTopSupervisor() {
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
        pr -> BehaviorCreator.create(pr, SupervisorProtocol.class, TopSupervisor::new),
        supervisionPoliciesForTopSupervisor());
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilderWithDefaultCommandHandlers().build();
  }

  private Behavior<Command> onUnhandledMessageTypeReceived(Command command) {
    context.getLog().info("unhandled command message type received: [" + command + "]");
    return Behaviors.unhandled();
  }

  @Override
  protected SubordinateSpawner<Command> spawnSubordinatesOnContextCreation() {
    return newSubordinateSpawner()
        .addSpawningBehavior(GraphQlProtocol.class, GraphQLSupervisor::create)
        .addSpawningBehavior(LookUpCommand.class, LookUpServicesSupervisor::create)
        .addSpawningBehavior(ModelProtocol.class, ModelProtocolSupervisor::create);
    //            .addSpawningBehavior(DatabaseCommand.class, DatabaseSupervisor::create)

  }

  @Override
  public ActorRefRegistrations<SupervisorProtocol> registrations() {
    return newRegistrationsBuilder().build();
  }

  @Override
  protected Behavior<Command> onSubordinateTerminated(SubordinateTerminated command) {
    context.getLog().info(String.format("subordinate_supervisor_terminated: %s", command));
    ActorPath actorPathOfTerminated = command.actorPathOfTerminatedSubordinate();
    // TODO: add logic for handling termination of a supervisor

    return Behaviors.stopped(() -> context.getSystem().terminate());
  }
}
