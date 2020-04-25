package io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa;

import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.JoinExpressionSupplier;
import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.RootJoinExpressionSupplier;
import org.immutables.value.Value;

@Value.Immutable
public interface RootJoinEdge extends JoinEdge {

  RootJoinExpressionSupplier rootJoinSupplier();

  @Override
  default JoinExpressionSupplier joinExpressionSupplier() {
    return rootJoinSupplier();
  }
}
