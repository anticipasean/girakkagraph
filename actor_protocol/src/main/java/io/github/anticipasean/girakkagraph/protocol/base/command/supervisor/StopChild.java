package io.github.anticipasean.girakkagraph.protocol.base.command.supervisor;

import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import java.util.Optional;

public interface StopChild extends Command<ChildStopped> {
  Optional<String> childName();
}
