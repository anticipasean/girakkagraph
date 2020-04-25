package io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

public interface PredicateSupplier extends ExpressionSupplier {

  default Predicate predicate() {
    return (Predicate) get();
  }

  @Override
  @SuppressWarnings("unchecked")
  default <X> Expression<X> expression() {
    return (Expression<X>) predicate();
  }
}
