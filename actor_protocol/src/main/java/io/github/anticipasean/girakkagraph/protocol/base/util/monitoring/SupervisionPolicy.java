package io.github.anticipasean.girakkagraph.protocol.base.util.monitoring;

import akka.actor.typed.SupervisorStrategy;
import org.immutables.value.Value;

@Value.Immutable
public interface SupervisionPolicy {

  int priority();

  SupervisorStrategy supervisorStrategy();

  Class<? extends Throwable> failureType();
}
