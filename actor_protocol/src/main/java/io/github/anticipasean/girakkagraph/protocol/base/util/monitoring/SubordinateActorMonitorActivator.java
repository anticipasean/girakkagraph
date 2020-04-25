package io.github.anticipasean.girakkagraph.protocol.base.util.monitoring;

import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.ActorContext;
import akka.japi.function.Function4;

public interface SubordinateActorMonitorActivator<C>
    extends Function4<ActorContext<C>, ActorRef<C>, Class<?>, String, Boolean> {
  @Override
  Boolean apply(
      ActorContext<C> context,
      ActorRef<C> subordinateActorRef,
      Class<?> registeredCommandType,
      String subordinateId)
      throws Exception;
}
