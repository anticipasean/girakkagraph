package io.github.anticipasean.girakkagraph.protocol.model.domain.operandset;

import io.github.anticipasean.girakkagraph.protocol.model.domain.context.Contextual;
import io.github.anticipasean.girakkagraph.protocol.model.domain.context.ProcessingContext;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operand.Operand;

public interface ContextualOperandSet<O extends Operand, P extends ProcessingContext>
    extends OperandSet<O>, Contextual<P> {}
