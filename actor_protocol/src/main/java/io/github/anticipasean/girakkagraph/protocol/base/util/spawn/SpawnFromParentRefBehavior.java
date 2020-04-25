package io.github.anticipasean.girakkagraph.protocol.base.util.spawn;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import java.util.function.Function;

@FunctionalInterface
public interface SpawnFromParentRefBehavior<C> extends Function<ActorRef<C>, Behavior<C>> {

  static <C> SpawnFromParentRefBehavior<C> from(Function<ActorRef<C>, Behavior<C>> function) {
    return function::apply;
  }
}
