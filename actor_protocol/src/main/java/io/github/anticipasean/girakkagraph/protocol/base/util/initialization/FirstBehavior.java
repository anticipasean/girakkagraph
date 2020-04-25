package io.github.anticipasean.girakkagraph.protocol.base.util.initialization;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.command.status.StartUp;
import java.util.function.BiFunction;

public interface FirstBehavior
    extends BiFunction<ActorContext<Command>, StartUp, Behavior<Command>> {}
