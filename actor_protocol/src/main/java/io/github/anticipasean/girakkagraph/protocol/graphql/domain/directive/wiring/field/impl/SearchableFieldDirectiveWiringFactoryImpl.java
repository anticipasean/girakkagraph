package io.github.anticipasean.girakkagraph.protocol.graphql.domain.directive.wiring.field.impl;

import com.google.common.collect.ImmutableMap;
import io.github.anticipasean.girakkagraph.protocol.graphql.domain.directive.wiring.field.BaseSearchableFieldDirectiveWiringImpl;
import io.github.anticipasean.girakkagraph.protocol.graphql.domain.directive.wiring.field.SearchableFieldDirectiveWiring;
import io.github.anticipasean.girakkagraph.protocol.graphql.domain.directive.wiring.field.SearchableFieldDirectiveWiringFactory;
import io.github.anticipasean.girakkagraph.protocol.graphql.domain.operator.GraphQLArgumentOperator;
import io.github.anticipasean.girakkagraph.protocol.graphql.domain.scalar.GraphQLScalarsSupport;
import graphql.schema.GraphQLScalarType;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SearchableFieldDirectiveWiringFactoryImpl
    implements SearchableFieldDirectiveWiringFactory {
  private final Set<GraphQLArgumentOperator> graphQLArgumentOperators;
  private final Map<String, GraphQLScalarType> scalarsMap;
  private final Map<String, Set<Class<?>>> coercibleJavaTypeSetMap;

  public SearchableFieldDirectiveWiringFactoryImpl(
      Set<GraphQLArgumentOperator> graphQLArgumentOperators) {
    this.graphQLArgumentOperators = graphQLArgumentOperators;
    this.scalarsMap = GraphQLScalarsSupport.allScalarsMap();
    coercibleJavaTypeSetMap =
        GraphQLScalarsSupport.graphQLScalarTypeNameToCoercibleJavaTypeSetMap();
  }

  @Override
  public Set<GraphQLArgumentOperator> graphQlArgumentOperators() {
    return graphQLArgumentOperators;
  }

  @Override
  public Map<String, SearchableFieldDirectiveWiring>
      createGraphQLTypeNameToSearchableFieldDirectiveWiringsMap() {
    return scalarsMap.entrySet().stream()
        .parallel()
        .unordered()
        .map(createScalarTypeNameTypeEntryFromSearchDirectiveWiring())
        .collect(
            Collectors.toConcurrentMap(
                SearchableFieldDirectiveWiring::graphQLTypeName, Function.identity()))
        .entrySet()
        .stream()
        .reduce(
            ImmutableMap.<String, SearchableFieldDirectiveWiring>builder(),
            (builder, wiringEntry) -> builder.put(wiringEntry.getKey(), wiringEntry.getValue()),
            (builder, builder2) -> builder2)
        .build();
  }

  private Function<Entry<String, GraphQLScalarType>, SearchableFieldDirectiveWiring>
      createScalarTypeNameTypeEntryFromSearchDirectiveWiring() {
    return entry -> {
      return (SearchableFieldDirectiveWiring)
          graphQLArgumentOperators.stream()
              .filter(
                  graphQLArgumentOperator ->
                      graphQLArgumentOperator.supportedGraphQLTypeNames().contains(entry.getKey()))
              .reduce(
                  BaseSearchableFieldDirectiveWiringImpl.builder()
                      .graphQLTypeName(entry.getKey())
                      .coercibleJavaTypes(
                          getCoercibleJavaTypesForGraphQLScalarType(entry.getValue())),
                  (builder, graphQLArgumentOperator) ->
                      builder.addCompatibleGraphQLArgumentOperator(graphQLArgumentOperator),
                  (builder, builder2) -> builder2)
              .build();
    };
  }

  public Set<Class<?>> getCoercibleJavaTypesForGraphQLScalarType(
      GraphQLScalarType graphQLScalarType) {
    String typeName = graphQLScalarType.getName();
    return coercibleJavaTypeSetMap.get(typeName);
  }
}
