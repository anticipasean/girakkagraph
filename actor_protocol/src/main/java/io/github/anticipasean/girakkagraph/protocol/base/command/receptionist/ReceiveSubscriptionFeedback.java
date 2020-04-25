package io.github.anticipasean.girakkagraph.protocol.base.command.receptionist;

import akka.actor.typed.receptionist.ServiceKey;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import org.immutables.value.Value;

@Value.Immutable
public interface ReceiveSubscriptionFeedback extends Command<SubscriptionFeedbackReceived> {
  ServiceKey<?> subscribedToUpdatesForRefs();
}
