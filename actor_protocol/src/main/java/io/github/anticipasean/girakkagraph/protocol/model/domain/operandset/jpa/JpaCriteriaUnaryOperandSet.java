package io.github.anticipasean.girakkagraph.protocol.model.domain.operandset.jpa;

import io.github.anticipasean.girakkagraph.protocol.model.domain.arity.Unary;
import org.immutables.value.Value;

@Value.Immutable
interface JpaCriteriaUnaryOperandSet extends JpaCriteriaOperandSet, Unary {}
