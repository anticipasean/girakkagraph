package io.github.anticipasean.girakkagraph.protocol.supervisor.command.lookup;

import akka.NotUsed;
import io.github.anticipasean.girakkagraph.protocol.supervisor.command.SupervisorProtocol;
import org.immutables.value.Value;

@Value.Immutable
public interface InitializeShareableRepository extends SupervisorProtocol<NotUsed> {}
