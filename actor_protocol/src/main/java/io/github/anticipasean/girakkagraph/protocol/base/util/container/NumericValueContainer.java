package io.github.anticipasean.girakkagraph.protocol.base.util.container;

import org.immutables.value.Value;

@Value.Immutable
public interface NumericValueContainer<N extends Number> extends ValueContainer<N, Number, Number> {

  @Override
  Class<N> type();

  @Override
  String name();

  @Override
  Class<Number> containerType();

  @Override
  Number value();
}
