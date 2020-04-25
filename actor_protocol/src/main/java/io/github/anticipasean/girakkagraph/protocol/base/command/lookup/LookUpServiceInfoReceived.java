package io.github.anticipasean.girakkagraph.protocol.base.command.lookup;

import akka.NotUsed;
import io.github.anticipasean.girakkagraph.protocol.base.util.lookup.LookUpServiceInfo;
import org.immutables.value.Value;

@Value.Immutable
public interface LookUpServiceInfoReceived extends LookUpCommand<NotUsed> {

  LookUpServiceInfo lookUpServiceInfo();
}
