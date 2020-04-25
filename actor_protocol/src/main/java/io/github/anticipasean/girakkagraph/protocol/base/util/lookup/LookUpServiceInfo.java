package io.github.anticipasean.girakkagraph.protocol.base.util.lookup;

import akka.actor.typed.ActorRef;
import io.github.anticipasean.girakkagraph.protocol.base.command.lookup.shareable.LookUpShareables;
import io.github.anticipasean.girakkagraph.protocol.base.command.lookup.shareable.RegisterDependencies;
import org.immutables.value.Value;

@Value.Immutable
public interface LookUpServiceInfo {

  ActorRef<LookUpShareables> lookUpDependencyActorRef();

  ActorRef<RegisterDependencies> registerDependenciesActorRef();
}
