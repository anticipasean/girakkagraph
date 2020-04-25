package io.github.anticipasean.girakkagraph.protocol.base.command.receptionist;

import akka.NotUsed;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.util.container.ActorRefValueContainer;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.Registrable;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
public interface RegisteredActorFound extends Command<NotUsed> {

  Registrable<?> registrableForActorInSearch();

  Optional<ActorRefValueContainer<?>> actorFoundMaybe();
}
