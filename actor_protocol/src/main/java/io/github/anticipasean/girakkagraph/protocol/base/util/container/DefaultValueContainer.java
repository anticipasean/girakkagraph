package io.github.anticipasean.girakkagraph.protocol.base.util.container;

import java.util.concurrent.atomic.AtomicReference;
import org.immutables.value.Value;

@Value.Immutable
public interface DefaultValueContainer<T>
    extends ValueContainer<T, AtomicReference, AtomicReference<T>> {
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
  default Class<AtomicReference> containerType() {
    return AtomicReference.class;
  }

  @Override
  AtomicReference<T> value();
}
