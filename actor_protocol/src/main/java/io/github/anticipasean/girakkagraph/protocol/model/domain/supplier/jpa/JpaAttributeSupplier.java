package io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa;

import javax.persistence.metamodel.Attribute;

public interface JpaAttributeSupplier<A extends Attribute<?, ?>> extends CriterionSupplier<A> {

  @SuppressWarnings("unchecked")
  default <X, T> Attribute<X, T> jpaAttribute() {
    return (Attribute<X, T>) get();
  }
}
