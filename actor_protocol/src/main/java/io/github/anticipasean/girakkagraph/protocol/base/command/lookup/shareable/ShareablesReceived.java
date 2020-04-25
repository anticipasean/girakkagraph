package io.github.anticipasean.girakkagraph.protocol.base.command.lookup.shareable;

import akka.NotUsed;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import org.immutables.value.Value;

@Value.Immutable
public interface ShareablesReceived extends Command<NotUsed> {}
