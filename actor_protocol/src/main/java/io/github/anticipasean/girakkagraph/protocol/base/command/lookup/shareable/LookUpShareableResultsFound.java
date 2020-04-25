package io.github.anticipasean.girakkagraph.protocol.base.command.lookup.shareable;

import akka.NotUsed;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.util.shareable.Shareable;
import java.util.List;
import org.immutables.value.Value;

@Value.Immutable
public interface LookUpShareableResultsFound extends Command<NotUsed> {
  List<Shareable> shareablesFound();
}
