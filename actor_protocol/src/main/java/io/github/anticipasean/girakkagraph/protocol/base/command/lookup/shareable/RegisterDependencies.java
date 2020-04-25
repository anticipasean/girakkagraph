package io.github.anticipasean.girakkagraph.protocol.base.command.lookup.shareable;

import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.util.dependencies.Dependencies;
import org.immutables.value.Value;

@Value.Immutable
public interface RegisterDependencies extends Command<DependenciesRegistered> {
  Dependencies dependencies();
}
