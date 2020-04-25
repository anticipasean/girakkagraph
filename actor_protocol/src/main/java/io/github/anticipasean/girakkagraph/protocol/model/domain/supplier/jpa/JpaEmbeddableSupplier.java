package io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa;

import javax.persistence.metamodel.EmbeddableType;

public interface JpaEmbeddableSupplier extends ManagedTypeSupplier<EmbeddableType<?>> {

  @SuppressWarnings("unchecked")
  default <X> EmbeddableType<X> embeddableType() {
    return (EmbeddableType<X>) get();
  }
}
