package io.github.anticipasean.girakkagraph.protocol.model.domain.operandset.jpa;

import io.github.anticipasean.girakkagraph.protocol.model.domain.arity.Binary;
import org.immutables.value.Value;

@Value.Immutable
interface JpaCriteriaBinaryOperandSet extends JpaCriteriaOperandSet, Binary {}
