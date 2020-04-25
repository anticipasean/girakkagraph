package io.github.anticipasean.girakkagraph.protocol.base.command.receptionist;

import akka.NotUsed;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import org.immutables.value.Value;

@Value.Immutable
public interface SubscriptionsStarted extends Command<NotUsed> {}
