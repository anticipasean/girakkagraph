package io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa;

import javax.persistence.criteria.Order;

public interface OrderSupplier extends CriterionSupplier<Order> {

  default Order jpaCriteriaOrderObject() {
    return get();
  }
}
