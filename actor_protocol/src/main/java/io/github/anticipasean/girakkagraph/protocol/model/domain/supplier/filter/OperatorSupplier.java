package io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.filter;

import io.github.anticipasean.girakkagraph.protocol.base.util.valuesupplier.ValueSupplier;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operator.Operator;

public interface OperatorSupplier extends ValueSupplier<Operator<?, ?>> {

  default Operator<?, ?> operator() {
    return get();
  }
}
