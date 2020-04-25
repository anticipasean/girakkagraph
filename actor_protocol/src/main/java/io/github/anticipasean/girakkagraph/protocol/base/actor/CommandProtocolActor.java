package io.github.anticipasean.girakkagraph.protocol.base.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.ChildFailed;
import akka.actor.typed.PostStop;
import akka.actor.typed.PreRestart;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.ReceiveBuilder;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.command.lookup.shareable.LookUpShareableResultsFound;
import io.github.anticipasean.girakkagraph.protocol.base.command.monitoring.SubordinateTerminated;
import io.github.anticipasean.girakkagraph.protocol.base.command.monitoring.SubordinateTerminatedImpl;
import io.github.anticipasean.girakkagraph.protocol.base.util.delegation.DelegateConfigurable;
import io.github.anticipasean.girakkagraph.protocol.base.util.delegation.StallingDelegatePlan;
import io.github.anticipasean.girakkagraph.protocol.base.util.delegation.WaitingDelegateActor;
import io.github.anticipasean.girakkagraph.protocol.base.util.delegation.WaitingDelegatePlan;
import io.github.anticipasean.girakkagraph.protocol.base.util.monitoring.RegistrableSubordinateActorMonitorActivator;
import io.github.anticipasean.girakkagraph.protocol.base.util.monitoring.SubordinateActorMonitorActivator;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.Spawnable;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SpawnedContext;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SubordinateSpawner;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.Registrable;
import java.util.function.Consumer;

public abstract class CommandProtocolActor<T extends Command> extends BaseActor<Command>
    implements SpawnedContext<Command, T>, DelegateConfigurable<Command> {
  private static final SubordinateActorMonitorActivator<Command> subordinateActorMonitorActivator =
      RegistrableSubordinateActorMonitorActivator.getInstance();
  private final SpawnedContext<Command, T> spawnedContext;

  protected CommandProtocolActor(SpawnedContext<Command, T> spawnedContext) {
    super(spawnedContext.context());
    this.spawnedContext = spawnedContext;
    context().setLoggerName(loggingName());
  }

  protected String loggingName() {
    return role().actorNamingBasedOnRole().apply(id());
  }

  protected <Rec extends Command<Res>, Res> void replyToIfPresent(
      Rec receivedCommand, Res responseCommand) {
    receivedCommand.replyTo().ifPresent(ref -> ref.tell(responseCommand));
  }

  @Override
  protected SubordinateSpawner<Command> newSubordinateSpawner() {
    return new SubordinateSpawner<>(context, subordinateActorMonitorActivator);
  }

  protected Behavior<Command> onPostStop(PostStop postStop) {
    context.getLog().debug("signal received: PostStop[" + postStop + "]");
    return Behaviors.same();
  }

  protected Behavior<Command> onPreRestart(PreRestart preRestart) {
    context.getLog().debug("signal received: PreRestart[" + preRestart + "]");
    return Behaviors.same();
  }

  protected Behavior<Command> onChildFailed(ChildFailed childFailed) {
    context.getLog().debug("signal received: ChildFailed[" + childFailed + "]");
    return Behaviors.same();
  }

  protected Behavior<Command> onTerminated(Terminated terminated) {
    context.getLog().debug("signal received: Terminated[" + terminated + "]");
    return Behaviors.same();
  }

  protected ReceiveBuilder<Command> newReceiveBuilderWithDefaultCommandHandlers() {
    return newReceiveBuilder()
        .onSignal(ChildFailed.class, this::onChildFailed)
        .onSignal(PreRestart.class, this::onPreRestart)
        .onSignal(Terminated.class, this::onTerminated)
        .onSignal(PostStop.class, this::onPostStop)
        .onMessage(SubordinateTerminated.class, this::onSubordinateTerminated)
        .onMessage(LookUpShareableResultsFound.class, this::onLookUpShareableResultsFound);
  }

  protected Behavior<Command> onLookUpShareableResultsFound(LookUpShareableResultsFound command) {
    context.getLog().info("look_up_shareables_found: " + command);
    context
        .getLog()
        .warn(
            "shareables requested are being received but not handled by this actor: "
                + selfRef()
                + "\nonLookUpShareableResultsFound should be overridden and implemented");
    return Behaviors.same();
  }

  protected Behavior<Command> onSubordinateTerminated(SubordinateTerminated command) {
    context.getLog().info("subordinate_terminated: " + command);
    return Behaviors.same();
  }

  @Override
  public ActorContext<Command> context() {
    return super.context();
  }

  @Override
  public ActorRef<Command> parentRef() {
    return spawnedContext.parentRef();
  }

  @Override
  public Registrable<T> registrable() {
    return spawnedContext.registrable();
  }

  @Override
  public Spawnable<Command, T> spawnable() {
    return spawnedContext.spawnable();
  }

  protected <Req, Res>
      ActorRef<WaitingDelegateActor.Response> letDelegateRequestAndWaitForResponseFollowingPlan(
          WaitingDelegatePlan<Command, Req, Res> waitingDelegatePlan) {
    return letDelegateRequestAndWaitForResponseFollowingPlanInContext(
        waitingDelegatePlan,
        context,
        (ref, plan) ->
            SubordinateTerminatedImpl.builder()
                .message(
                    ref
                        + String.format(
                            " responsible for handling %s response has been terminated.",
                            plan.forResponseOfType().getName()))
                .actorPathOfTerminatedSubordinate(ref.path())
                .registeredCommandType(plan.forResponseOfType())
                .id(plan.delegateName())
                .build());
  }

  protected <Req, Cond> Consumer<Req> letDelegateStashRequestsAndStallRespondingToThemFollowingPlan(
      StallingDelegatePlan<Req, Cond> stallingDelegatePlan) {
    return letDelegateStashRequestsAndStallRespondingToThemFollowingPlanInContext(
        stallingDelegatePlan,
        context,
        (ref, plan) ->
            SubordinateTerminatedImpl.builder()
                .message(
                    ref
                        + String.format(
                            " responsible for handling %s response has been terminated.",
                            plan.stallRespondingToType().getSimpleName()))
                .actorPathOfTerminatedSubordinate(ref.path())
                .registeredCommandType(plan.stallRespondingToType())
                .id(plan.delegateName())
                .build());
  }
}
