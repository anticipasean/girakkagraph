package io.github.anticipasean.girakkagraph.protocol.base.util.delegation;

import akka.actor.typed.ActorRef;

public interface DelegatePlan<Ctx> {

  String delegateName();

  ActorRef<Ctx> onBehalfOf();
}
