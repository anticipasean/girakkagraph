package io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa;

import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;

public interface FromObjectSupplier extends JpaPathSupplier {

  @SuppressWarnings("unchecked")
  default <Z, X> From<Z, X> fromObject() {
    return (From<Z, X>) get();
  }

  @Override
  default <X> Path<X> jpaPath() {
    return fromObject();
  }
}
