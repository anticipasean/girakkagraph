package io.github.anticipasean.girakkagraph.protocol.base.util.spawn;

import akka.actor.typed.Behavior;
import java.util.function.Function;

@FunctionalInterface
public interface SpawnFromContextBehavior<C, T>
    extends Function<SpawnedContext<C, T>, Behavior<C>> {

  static <C, T> SpawnFromContextBehavior<C, T> from(
      Function<SpawnedContext<C, T>, Behavior<C>> function) {
    return function::apply;
  }
}
