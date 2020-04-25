package io.github.anticipasean.girakkagraph.protocol.model.domain.graph;

import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.vertex.RootVertex;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
public interface BaseModelGraph extends ModelGraph {
  @Value.Derived
  default Optional<RootVertex> rootVertexOpt() {
    return vertices().values().stream()
        .filter(modelVertex -> modelVertex instanceof RootVertex)
        .map(modelVertex -> (RootVertex) modelVertex)
        .findFirst();
  }
}
