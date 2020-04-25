package io.github.anticipasean.girakkagraph.protocol.model.domain.graph.vertex;

import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.ModelVertex;
import java.util.function.Supplier;
import org.immutables.value.Value;

public interface ParameterVertex extends ModelVertex {

  @Value.Check
  default void checkParameterVertexPath() {
    if (vertexPath().rawArguments().size() == 0 && vertexPath().directives().size() == 0) {
      Supplier<String> messageSupplier =
          () ->
              String.format(
                  "a path for a parameter vertex must have at least "
                      + "one corresponding argument or directive [ %s ]",
                  vertexPath());
      throw new IllegalArgumentException(messageSupplier.get());
    }
  }

}
