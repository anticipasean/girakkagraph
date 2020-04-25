package io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa;

import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.BranchJoinExpressionSupplier;
import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.JoinExpressionSupplier;
import org.immutables.value.Value;

@Value.Immutable
public interface BranchJoinEdge extends JoinEdge {

  BranchJoinExpressionSupplier branchJoinSupplier();

  @Override
  default JoinExpressionSupplier joinExpressionSupplier() {
    return branchJoinSupplier();
  }
}
