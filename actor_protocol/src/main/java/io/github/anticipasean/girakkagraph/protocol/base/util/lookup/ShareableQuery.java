package io.github.anticipasean.girakkagraph.protocol.base.util.lookup;

import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
public interface ShareableQuery {

  @Value.Parameter
  String name();

  @Value.Parameter
  Class<?> valueContainerType();

  @Value.Parameter
  Class<?> registeredType();

  Optional<String> fullJavaClassNameWithParamsIfKnown();
}
