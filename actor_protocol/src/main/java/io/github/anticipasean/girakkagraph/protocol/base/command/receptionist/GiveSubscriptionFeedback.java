package io.github.anticipasean.girakkagraph.protocol.base.command.receptionist;

import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import org.immutables.value.Value;

@Value.Immutable
public interface GiveSubscriptionFeedback extends Command<TakeSubscriptionFeedback> {}
