package io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa;

import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.vertex.RootVertex;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.ModelPath;
import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.FromObjectSupplier;
import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.RootFromExpressionSupplier;
import org.immutables.value.Value;

@Value.Immutable
public interface RootFromVertex extends RootVertex, FromObjectEdge {

  @Override
  default ModelPath parentPath() {
    return vertexPath();
  }

  @Override
  default ModelPath childPath() {
    return vertexPath();
  }

  @Override
  default void checkParentPathIsParentToChildPath() {
    // Overriding since this restriction does not apply to root
  }

  RootFromExpressionSupplier rootFromExpressionSupplier();

  @Override
  default FromObjectSupplier fromObjectSupplier() {
    return rootFromExpressionSupplier();
  }
}
