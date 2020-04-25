package io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa;

import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.FromObjectSupplier;
import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.JoinExpressionSupplier;
import javax.persistence.criteria.JoinType;

public interface JoinEdge extends FromObjectEdge {

  JoinType joinType();

  JoinExpressionSupplier joinExpressionSupplier();

  @Override
  default FromObjectSupplier fromObjectSupplier() {
    return joinExpressionSupplier();
  }
}
