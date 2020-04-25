package io.github.anticipasean.girakkagraph.protocol.model.domain.graph.vertex;

import java.util.function.Supplier;
import org.immutables.value.Value;

public interface RootVertex extends TypeVertex {

  @Value.Check
  default void checkRootVertexPath() {
    if (vertexPath().depth() != 1) {
      Supplier<String> messageSupplier =
          () ->
              String.format(
                  "a path for a root vertex can only have one path segment,"
                      + " the top level entity for the query [ %s ]",
                  vertexPath());
      throw new IllegalArgumentException(messageSupplier.get());
    }
  }
}
