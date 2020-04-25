package io.github.anticipasean.girakkagraph.protocol.base.util.container;

import akka.actor.typed.receptionist.ServiceKey;
import org.immutables.value.Value;

@Value.Immutable
public interface ServiceKeyValueContainer<T> extends ValueContainer<T, ServiceKey, ServiceKey<T>> {
  @Override
  Class<T> type();

  @Override
  @Value.Default
  default String name() {
    return new StringBuilder(containerType().getName())
        .append("<")
        .append(type().getName())
        .append(">")
        .toString();
  }

  @Override
  default Class<ServiceKey> containerType() {
    return ServiceKey.class;
  }

  @Override
  ServiceKey<T> value();
}
