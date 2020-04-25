package io.github.anticipasean.girakkagraph.protocol.base.command.receptionist;

import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.Registrable;
import org.immutables.value.Value;

@Value.Immutable
public interface SearchForRegisteredActor extends Command<RegisteredActorFound> {
  Registrable<?> registrable();
}
