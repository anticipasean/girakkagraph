package io.github.anticipasean.girakkagraph.protocol.base.util.initialization;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.command.lookup.LookUpServiceAvailable;
import io.github.anticipasean.girakkagraph.protocol.base.command.lookup.LookUpServiceInfoReceived;
import io.github.anticipasean.girakkagraph.protocol.base.command.lookup.shareable.BuildShareables;
import io.github.anticipasean.girakkagraph.protocol.base.command.lookup.shareable.DependenciesRegistered;
import io.github.anticipasean.girakkagraph.protocol.base.command.lookup.shareable.RegisterDependencies;
import io.github.anticipasean.girakkagraph.protocol.base.command.lookup.shareable.ShareablesBuilt;
import io.github.anticipasean.girakkagraph.protocol.base.command.lookup.shareable.ShareablesReceived;
import io.github.anticipasean.girakkagraph.protocol.base.command.lookup.shareable.TakeShareables;
import io.github.anticipasean.girakkagraph.protocol.base.command.status.StartUp;
import io.github.anticipasean.girakkagraph.protocol.base.util.dependencies.Dependencies;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.Subordinate;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.immutables.value.Value;

@Value.Style(
    typeImmutable = "*Impl",
    typeAbstract = "*",
    overshadowImplementation = true,
    stagedBuilder = true)
@Value.Immutable
public interface StartUpSequence {

  BiFunction<ActorContext<Command>, StartUp, Behavior<Command>> whenStartUpReceived();

  Function<ActorRef<LookUpServiceInfoReceived>, LookUpServiceAvailable> checkForLookUpServiceInfo();

  BiFunction<ActorRef<DependenciesRegistered>, Dependencies, RegisterDependencies>
      registerDependencies();

  Function<ActorRef<ShareablesBuilt>, BuildShareables> buildShareables();

  BiFunction<ShareablesBuilt, ActorRef<ShareablesReceived>, TakeShareables> shareShareablesBuilt();

  Function<
          List<Subordinate<Command>>, BiFunction<ActorContext<Command>, StartUp, Behavior<Command>>>
      startUpSubordinates();

  BiFunction<ActorContext<Command>, StartUp, Behavior<Command>> finishStartUpSequence();
}
