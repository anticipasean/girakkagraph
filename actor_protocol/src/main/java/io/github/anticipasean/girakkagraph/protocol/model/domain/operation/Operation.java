package io.github.anticipasean.girakkagraph.protocol.model.domain.operation;

import io.github.anticipasean.girakkagraph.protocol.model.domain.operandset.OperandSet;
import java.util.function.Function;

public interface Operation<O extends OperandSet, R> extends Function<O, R> {}
