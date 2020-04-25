package io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa;

import javax.persistence.criteria.Expression;

public interface ExpressionSupplier extends CriterionSupplier<Expression<?>> {

  @SuppressWarnings("unchecked")
  default <X> Expression<X> expression() {
    return (Expression<X>) get();
  }
}
