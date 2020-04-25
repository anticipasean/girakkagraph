package io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge;

import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.ModelEdge;
import org.immutables.value.Value;

@Value.Immutable
public interface DirectiveEdge extends ModelEdge {

  String name();
}
