package io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa;

import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.JpaSelectionSupplier;
import org.immutables.value.Value;

@Value.Immutable
public interface SelectionEdge extends JpaVertexRelationshipEdge {

  JpaSelectionSupplier selectionSupplier();
}
