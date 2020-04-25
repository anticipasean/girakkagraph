package io.github.anticipasean.girakkagraph.protocol.base.command.status;

import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import org.immutables.value.Value;

@Value.Immutable
public interface StartUp extends Command<Initialized> {}
