package io.github.anticipasean.girakkagraph.protocol.graphql.domain.directive.wiring.field;

import io.github.anticipasean.girakkagraph.protocol.graphql.domain.directive.wiring.field.impl.SearchableFieldDirectiveWiringFactoryImpl;
import io.github.anticipasean.girakkagraph.protocol.graphql.domain.operator.GraphQLArgumentOperator;
import java.util.Map;
import java.util.Set;

public interface SearchableFieldDirectiveWiringFactory {

  static SearchableFieldDirectiveWiringFactory newInstanceUsingGraphQLArgumentOperatorSet(
      Set<GraphQLArgumentOperator> graphQLArgumentOperators) {
    return new SearchableFieldDirectiveWiringFactoryImpl(graphQLArgumentOperators);
  }

  Set<GraphQLArgumentOperator> graphQlArgumentOperators();

  Map<String, SearchableFieldDirectiveWiring> createGraphQLTypeNameToSearchableFieldDirectiveWiringsMap();
}
