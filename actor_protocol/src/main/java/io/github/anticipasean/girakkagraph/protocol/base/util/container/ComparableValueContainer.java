package io.github.anticipasean.girakkagraph.protocol.base.util.container;

import org.immutables.value.Value;

@Value.Immutable
public interface ComparableValueContainer<T> extends ValueContainer<T, Comparable, Comparable<T>> {
  @Override
  Class<T> type();

  @Override
  @Value.Derived
  default String name() {
    return new StringBuilder(Comparable.class.getName())
        .append("<")
        .append(type().getName())
        .append(">")
        .toString();
  }

  @Override
  @Value.Default
  default Class<Comparable> containerType() {
    return Comparable.class;
  }

  @Override
  Comparable<T> value();
}
