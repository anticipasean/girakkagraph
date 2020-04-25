package io.github.anticipasean.girakkagraph.protocol.base.command.monitoring;

import akka.NotUsed;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import java.time.Duration;
import org.immutables.value.Value;

@Value.Immutable
public interface TimeoutReceived extends Command<NotUsed> {
  Duration timedOutAfter();
}
