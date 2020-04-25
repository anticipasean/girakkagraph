package io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;

public interface JpaPathSupplier extends ExpressionSupplier {

  @SuppressWarnings("unchecked")
  default <X> Path<X> jpaPath() {
    return (Path<X>) get();
  }

  @Override
  default <X> Expression<X> expression() {
    return jpaPath();
  }
}
