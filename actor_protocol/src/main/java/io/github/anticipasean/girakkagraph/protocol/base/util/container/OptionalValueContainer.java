package io.github.anticipasean.girakkagraph.protocol.base.util.container;

import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
public interface OptionalValueContainer<T> extends ValueContainer<T, Optional, Optional<T>> {

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
  Optional<T> value();

  @Override
  default Class<Optional> containerType() {
    return Optional.class;
  }

  //
  //  @Override
  //  default Function<? extends SimpleKey<T>, Optional<T>> yieldValueFunc(){
  //    return (simpleKey) -> {
  //      return (Optional<T>) value();
  //    };
  //  }

}
