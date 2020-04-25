package io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa;

import java.util.function.Function;

public interface FunctionalOrderSupplier extends FunctionalCriterionSupplier<Function<String,OrderSupplier>, String, OrderSupplier> {

  @Override
  default Class<String> inputType(){
    return String.class;
  }

  @Override
  default Class<OrderSupplier> outputType(){
    return OrderSupplier.class;
  }
}
