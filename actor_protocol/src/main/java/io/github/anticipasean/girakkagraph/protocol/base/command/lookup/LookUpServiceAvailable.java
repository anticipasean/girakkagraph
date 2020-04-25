package io.github.anticipasean.girakkagraph.protocol.base.command.lookup;

import org.immutables.value.Value;

@Value.Immutable
public interface LookUpServiceAvailable extends LookUpCommand<LookUpServiceInfoReceived> {}
