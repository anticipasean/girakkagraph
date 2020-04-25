package io.github.anticipasean.girakkagraph.protocol.model.domain.operand;

import io.github.anticipasean.girakkagraph.protocol.model.domain.operandset.OperandSet;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operator.Restriction;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(typeImmutable = "*Impl", overshadowImplementation = true, stagedBuilder = true)
public interface OperandRestriction<O extends OperandSet<?>> extends Restriction<O> {

  @Override
  default Unit onUnit() {
    return Unit.OPERAND;
  }

  Position inPosition();

  @Value.Derived
  @Override
  default String description() {
    return new StringBuilder("restriction_on_")
        .append(onUnit())
        .append("_of_")
        .append(ofType().name())
        .append("_in_position_")
        .append(inPosition().name())
        .append("_that_it_meets_condition_")
        .append(condition().toString())
        .toString();
  }

  enum Position {
    NONE(-1),
    FIELD(0),
    FIRST_VALUE(1),
    SECOND_VALUE(2),
    ALL(Integer.MAX_VALUE);
    private final int index;

    Position(int index) {
      this.index = index;
    }

    public int index() {
      return this.index;
    }
  }
}
