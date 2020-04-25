package io.github.anticipasean.girakkagraph.protocol.graphql.domain.operator;

import io.github.anticipasean.girakkagraph.protocol.model.domain.arity.ArityRestricted;
import graphql.schema.GraphQLArgument;
import java.util.Set;
import java.util.function.UnaryOperator;
import org.immutables.value.Value.Default;
import org.immutables.value.Value.Derived;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Style;

@Immutable
@Style(typeImmutable = "*Impl", overshadowImplementation = true, depluralize = true)
public interface GraphQLArgumentOperator extends ArityRestricted {

  String argumentName();

  @Derived
  default String description() {
    return "";
  }

  Set<String> supportedGraphQLTypeNames();

  @Default
  default UnaryOperator<GraphQLArgument> onApplyOperatorSpecificChangesToGraphQLArgument() {
    return UnaryOperator.identity();
  }
}
