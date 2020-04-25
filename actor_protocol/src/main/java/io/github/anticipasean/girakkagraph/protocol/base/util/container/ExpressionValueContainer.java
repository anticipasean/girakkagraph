package io.github.anticipasean.girakkagraph.protocol.base.util.container;

import javax.persistence.criteria.Expression;
import org.immutables.value.Value;

@Value.Immutable
public interface ExpressionValueContainer<X> extends ValueContainer<X, Expression, Expression<X>> {
  @Override
  Class<X> type();

  @Override
  @Value.Default
  default String name() {
    return new StringBuilder(containerType().getName())
        .append("<")
        .append(type().getName())
        .append(">")
        .toString();
  }

  @Override
  @Value.Default
  default Class<Expression> containerType() {
    return Expression.class;
  }

  @Override
  Expression<X> value();
}
