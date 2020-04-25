package io.github.anticipasean.girakkagraph.protocol.model.domain.operator.jpa;

import com.google.common.collect.ImmutableSet;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operandset.GenericRestrictions;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operandset.OperandSet;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operandset.jpa.JpaCriteriaOperandSet;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operator.Operator;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operator.Restriction;
import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.CriterionSupplier;
import java.util.Set;

public interface JpaCriteriaOperator<R extends CriterionSupplier>
    extends Operator<JpaCriteriaOperandSet, R> {

  @Override
  default Class<JpaCriteriaOperandSet> operandSetType() {
    return JpaCriteriaOperandSet.class;
  }

  @Override
  default Set<Restriction<OperandSet>> generalRestrictions() {
    return ImmutableSet.of(
        GenericRestrictions.operandSetMustBeOfType(JpaCriteriaOperandSet.class),
        GenericRestrictions.operandSetMustBeSize(arity().operandCount()));
  }

  @Override
  default Set<Restriction<?>> functionalParameterRestrictions(){
    return ImmutableSet.of();
  }
}
