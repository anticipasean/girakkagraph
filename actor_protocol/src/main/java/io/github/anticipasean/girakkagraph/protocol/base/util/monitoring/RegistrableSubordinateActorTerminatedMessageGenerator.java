package io.github.anticipasean.girakkagraph.protocol.base.util.monitoring;

import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.ActorContext;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.command.monitoring.SubordinateTerminatedImpl;

public class RegistrableSubordinateActorTerminatedMessageGenerator
    implements SubordinateActorTerminatedMessageGenerator<Command> {
  @Override
  public Command apply(
      ActorContext<Command> context,
      ActorRef<Command> subordinateActorRef,
      Class<?> registeredCommandType,
      String subordinateId)
      throws Exception {
    return SubordinateTerminatedImpl.builder()
        .actorPathOfTerminatedSubordinate(subordinateActorRef.path())
        .message(
            String.format(
                "subordinate [ actorRef: %s, id: %s, handles: %s ] to [ actorRef: %s ] has terminated",
                subordinateActorRef,
                subordinateId,
                registeredCommandType.getSimpleName(),
                context.getSelf()))
        .registeredCommandType(registeredCommandType)
        .id(subordinateId)
        .build();
  }
}
