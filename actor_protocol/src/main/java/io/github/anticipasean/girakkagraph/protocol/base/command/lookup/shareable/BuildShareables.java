package io.github.anticipasean.girakkagraph.protocol.base.command.lookup.shareable;

import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.util.shareable.Shareables;
import org.immutables.value.Value;

@Value.Immutable
public interface BuildShareables extends Command<ShareablesBuilt> {

  Shareables shareables();
}
