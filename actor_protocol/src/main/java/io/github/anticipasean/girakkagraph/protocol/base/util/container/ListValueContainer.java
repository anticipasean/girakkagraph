package io.github.anticipasean.girakkagraph.protocol.base.util.container;

import java.util.List;
import org.immutables.value.Value;

@Value.Immutable
public interface ListValueContainer<E> extends ValueContainer<E, List, List<E>> {

  @Override
  Class<E> type();

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
  default Class<List> containerType() {
    return List.class;
  }

  //  @Override
  //  default Function<? extends ListKey<E>, List<E>> yieldValueFunc(){
  //    return (key) -> {
  //      return (List<E>) value();
  //    };
  //  }
  @Override
  List<E> value();
}
