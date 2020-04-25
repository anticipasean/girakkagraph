package io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa;

import akka.japi.Pair;
import java.util.function.Function;

public interface FunctionalGroupByExpressionSupplier
    extends FunctionalCriterionSupplier<
        Function<Integer, FunctionalGroupByExpressionSupplier.IndexedExpressionSupplier>,
        Integer,
        FunctionalGroupByExpressionSupplier.IndexedExpressionSupplier> {

  @Override
  default Class<Integer> inputType() {
    return Integer.class;
  }

  @Override
  default Class<IndexedExpressionSupplier> outputType() {
    return IndexedExpressionSupplier.class;
  }

  static interface IndexedExpressionSupplier
      extends CriterionSupplier<Pair<Integer, ExpressionSupplier>> {}
}
