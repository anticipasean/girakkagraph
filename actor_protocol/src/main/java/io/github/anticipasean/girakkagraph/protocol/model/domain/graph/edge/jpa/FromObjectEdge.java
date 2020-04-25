package io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa;

import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.FromObjectSupplier;

public interface FromObjectEdge extends JpaVertexRelationshipEdge {

  FromObjectSupplier fromObjectSupplier();
}
