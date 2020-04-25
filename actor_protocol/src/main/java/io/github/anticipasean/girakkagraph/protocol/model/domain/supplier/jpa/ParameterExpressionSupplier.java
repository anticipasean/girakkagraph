package io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa;

import javax.persistence.criteria.ParameterExpression;

public interface ParameterExpressionSupplier extends ExpressionSupplier {

  @SuppressWarnings("unchecked")
  default <X> ParameterExpression<X> parameterExpression() {
    return (ParameterExpression<X>) get();
  }
}
