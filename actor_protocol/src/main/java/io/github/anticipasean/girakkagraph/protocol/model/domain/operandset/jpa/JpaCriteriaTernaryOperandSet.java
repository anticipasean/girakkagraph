package io.github.anticipasean.girakkagraph.protocol.model.domain.operandset.jpa;

import io.github.anticipasean.girakkagraph.protocol.model.domain.arity.Ternary;
import org.immutables.value.Value;

@Value.Immutable
interface JpaCriteriaTernaryOperandSet extends JpaCriteriaOperandSet, Ternary {}
