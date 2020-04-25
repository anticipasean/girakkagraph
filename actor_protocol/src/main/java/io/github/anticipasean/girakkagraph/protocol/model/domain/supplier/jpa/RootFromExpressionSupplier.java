package io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa;

import javax.persistence.criteria.Root;

public interface RootFromExpressionSupplier extends FromObjectSupplier {

  @SuppressWarnings("unchecked")
  default <R> Root<R> rootFromObject() {
    return (Root<R>) get();
  }
}
