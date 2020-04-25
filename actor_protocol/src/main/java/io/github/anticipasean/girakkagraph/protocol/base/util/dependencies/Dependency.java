package io.github.anticipasean.girakkagraph.protocol.base.util.dependencies;

import io.github.anticipasean.girakkagraph.protocol.base.util.container.ValueContainer;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
public interface Dependency {

  String name();

  Class<?> type();

  Class<? extends ValueContainer> valueContainerType();

  Optional<String> fullJavaClassNameWithParameterTypesIfKnown();
}
