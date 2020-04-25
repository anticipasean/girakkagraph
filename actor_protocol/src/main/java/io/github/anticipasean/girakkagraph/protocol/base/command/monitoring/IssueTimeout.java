package io.github.anticipasean.girakkagraph.protocol.base.command.monitoring;

import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import java.time.Duration;
import org.immutables.value.Value;

@Value.Immutable
public interface IssueTimeout extends Command<TimeoutReceived> {
  Duration timeOutSetting();
}
