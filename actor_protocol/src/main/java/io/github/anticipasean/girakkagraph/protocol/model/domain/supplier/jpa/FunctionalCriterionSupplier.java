package io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa;

import java.util.function.Function;

public interface FunctionalCriterionSupplier<V extends Function<I, O>, I, O> extends CriterionSupplier<V> {

  Class<I> inputType();

  Class<O> outputType();

}
