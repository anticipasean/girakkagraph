package io.github.anticipasean.girakkagraph.protocol.base.command.lookup.shareable;

import io.github.anticipasean.girakkagraph.protocol.base.command.lookup.LookUpCommand;
import io.github.anticipasean.girakkagraph.protocol.base.util.shareable.ShareableRepository;
import org.immutables.value.Value;

@Value.Immutable
public interface ProvideShareableRepository extends LookUpCommand<ShareableRepository> {}
