package io.github.anticipasean.girakkagraph.protocol.base.util.container;

import org.immutables.value.Value;

@Value.Immutable
public interface ClassValueContainer extends ValueContainer<Class, Class, Class<?>> {
  @Override
  default Class<Class> type() {
    return Class.class;
  }

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
  default Class<Class> containerType() {
    return Class.class;
  }

  @Override
  Class<?> value();
}
