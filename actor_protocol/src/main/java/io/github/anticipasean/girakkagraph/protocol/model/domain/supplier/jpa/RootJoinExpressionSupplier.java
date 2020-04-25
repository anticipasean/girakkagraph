package io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa;

import javax.persistence.criteria.Root;

public interface RootJoinExpressionSupplier extends JoinExpressionSupplier {

  @SuppressWarnings("unchecked")
  default <R> Root<R> rootFromObject() {
    return (Root<R>) get();
  }
}
