package io.github.anticipasean.girakkagraph.protocol.model.domain.argument.jpa;

import io.github.anticipasean.girakkagraph.protocol.model.domain.argument.ModelArgument;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operandset.jpa.JpaCriteriaOperandSet;
import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.CriterionSupplier;

public interface CriteriaJpaArgument<R extends CriterionSupplier>
    extends ModelArgument<JpaCriteriaOperandSet, R> {}
