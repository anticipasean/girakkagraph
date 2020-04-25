package io.github.anticipasean.girakkagraph.protocol.model.domain.operandset;

import io.github.anticipasean.girakkagraph.protocol.model.domain.operator.Restriction;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(typeImmutable = "*Impl", overshadowImplementation = true, stagedBuilder = true)
public interface OperandSetRestriction<O extends OperandSet<?>> extends Restriction<O> {

  @Override
  default Unit onUnit() {
    return Unit.OPERAND_SET;
  }

  @Value.Derived
  @Override
  default String description() {
    return new StringBuilder("restriction_on_")
        .append(onUnit())
        .append("_of_")
        .append(ofType().name())
        .append("_that_it_meets_condition_")
        .append(condition().toString())
        .toString();
  }

  //  static <O extends OperandSet<?>> Restriction<O> of(String name, Predicate<O> condition) {
  //    return new OperandSetRestrictionImpl<>(name, condition);
  //  }
  //  class OperandSetRestrictionImpl<O extends OperandSet<?>> implements Restriction<O> {
  //    private final String name;
  //    private final Predicate<O> condition;
  //
  //    public OperandSetRestrictionImpl(String name, Predicate<O> condition) {
  //      this.name = name;
  //      this.condition = condition;
  //    }
  //
  //    @Override
  //    public String name() {
  //      return name;
  //    }
  //
  //    @Override
  //    public Predicate<O> operandSetCondition() {
  //      return condition;
  //    }
  //
  //    @Override
  //    public boolean equals(Object o) {
  //      if (this == o) return true;
  //      if (o == null || getClass() != o.getClass()) return false;
  //
  //      OperandSetRestrictionImpl<?> that = (OperandSetRestrictionImpl<?>) o;
  //
  //      if (!name.equals(that.name)) return false;
  //      return condition.equals(that.condition);
  //    }
  //
  //    @Override
  //    public int hashCode() {
  //      int result = name.hashCode();
  //      result = 31 * result + condition.hashCode();
  //      return result;
  //    }
  //  }
}
