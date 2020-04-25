package io.github.anticipasean.girakkagraph.protocol.model.domain.graph.vertex;

import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.ModelVertex;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.type.PersistableGraphQLType;
import java.util.function.Supplier;
import org.immutables.value.Value;

public interface TypeVertex extends ModelVertex {

  //  default <T> ManagedType<T> managedType() {
  //    @SuppressWarnings("unchecked")
  //    ManagedType<T> managedType = (ManagedType<T>) persistableGraphQlType().jpaManagedType();
  //    return managedType;
  //  }

  PersistableGraphQLType persistableGraphQlType();

  @Value.Check
  default void checkTypeVertexPath() {
    if (vertexPath().rawArguments().size() > 0) {
      Supplier<String> messageSupplier =
          () ->
              String.format(
                  "a path for a type vertex may not have any arguments [ %s ]", vertexPath());
      throw new IllegalArgumentException(messageSupplier.get());
    }
    if (vertexPath().directives().size() > 0) {
      Supplier<String> messageSupplier =
          () ->
              String.format(
                  "a path for a type vertex may not have any directives [ %s ]", vertexPath());
      throw new IllegalArgumentException(messageSupplier.get());
    }
  }
}
