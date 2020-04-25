package io.github.anticipasean.girakkagraph.protocol.model.domain.operator;

import org.immutables.value.Value;

@Value.Immutable
@Value.Style(typeImmutable = "*Impl", overshadowImplementation = true, stagedBuilder = true)
public interface FunctionalParameterRestriction<T> extends Restriction<T> {
  @Override
  default Unit onUnit() {
    return Unit.FUNCTIONAL_PARAMETER;
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
}
