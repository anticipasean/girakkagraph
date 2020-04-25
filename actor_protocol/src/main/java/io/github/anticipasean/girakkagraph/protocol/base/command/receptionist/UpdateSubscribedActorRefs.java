package io.github.anticipasean.girakkagraph.protocol.base.command.receptionist;

import akka.NotUsed;
import akka.actor.typed.receptionist.Receptionist;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import org.immutables.value.Value;

@Value.Immutable
public interface UpdateSubscribedActorRefs extends Command<NotUsed> {

  Receptionist.Listing listingReceived();
}
