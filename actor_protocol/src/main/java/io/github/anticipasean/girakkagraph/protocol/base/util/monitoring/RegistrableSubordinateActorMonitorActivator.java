package io.github.anticipasean.girakkagraph.protocol.base.util.monitoring;

import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.ActorContext;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;

public class RegistrableSubordinateActorMonitorActivator
    implements SubordinateActorMonitorActivator<Command> {

  private SubordinateActorTerminatedMessageGenerator<Command>
      subordinateActorTerminatedMessageGenerator;

  public RegistrableSubordinateActorMonitorActivator(
      SubordinateActorTerminatedMessageGenerator<Command>
          subordinateActorTerminatedMessageGenerator) {
    this.subordinateActorTerminatedMessageGenerator = subordinateActorTerminatedMessageGenerator;
  }

  public static SubordinateActorMonitorActivator<Command> getInstance() {
    return new RegistrableSubordinateActorMonitorActivator(
        new RegistrableSubordinateActorTerminatedMessageGenerator());
  }

  @Override
  public Boolean apply(
      ActorContext<Command> context,
      ActorRef<Command> subordinateActorRef,
      Class<?> registeredCommandType,
      String subordinateId)
      throws Exception {
    try {
      context.watchWith(
          subordinateActorRef,
          subordinateActorTerminatedMessageGenerator.apply(
              context, subordinateActorRef, registeredCommandType, subordinateId));
      return Boolean.TRUE;
    } catch (Exception e) {
      context
          .getLog()
          .error(
              String.format(
                  "unable to create subordinate actor terminated message for actor ref: %s",
                  subordinateActorRef));
      return Boolean.FALSE;
    }
  }
}
