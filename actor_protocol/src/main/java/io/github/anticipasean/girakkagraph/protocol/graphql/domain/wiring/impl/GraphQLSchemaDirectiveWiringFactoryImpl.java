package io.github.anticipasean.girakkagraph.protocol.graphql.domain.wiring.impl;

import io.github.anticipasean.girakkagraph.protocol.graphql.domain.directive.wiring.FocusedDirectiveWiring;
import io.github.anticipasean.girakkagraph.protocol.graphql.domain.directive.wiring.FocusedDirectiveWiringProvider;
import io.github.anticipasean.girakkagraph.protocol.graphql.domain.directive.wiring.field.SearchableFieldDefinitionDirectiveWiringProvider;
import io.github.anticipasean.girakkagraph.protocol.graphql.domain.directive.wiring.field.SearchableFieldDefinitionDirectiveWiringProviderImpl;
import io.github.anticipasean.girakkagraph.protocol.graphql.domain.directive.wiring.field.SearchableFieldDirectiveWiring;
import io.github.anticipasean.girakkagraph.protocol.graphql.domain.directive.wiring.field.SearchableFieldDirectiveWiringFactory;
import io.github.anticipasean.girakkagraph.protocol.graphql.domain.directive.wiring.scalar.CustomScalarFieldDefDirectiveWiringProvider;
import io.github.anticipasean.girakkagraph.protocol.graphql.domain.operator.GraphQLArgumentOperator;
import io.github.anticipasean.girakkagraph.protocol.graphql.domain.operator.GraphQLArgumentOperatorFactory;
import io.github.anticipasean.girakkagraph.protocol.graphql.domain.wiring.GraphQLSchemaDirectiveWiringFactory;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operator.OperatorDatabase;
import io.github.anticipasean.girakkagraph.typematcher.TypeMatcher;
import graphql.schema.GraphQLDirectiveContainer;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.idl.SchemaDirectiveWiring;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphQLSchemaDirectiveWiringFactoryImpl
    implements GraphQLSchemaDirectiveWiringFactory {

  private static final SchemaDirectiveWiring NO_MATCHING_DIRECTIVE_WIRING_RESULT =
      new SchemaDirectiveWiring() {};
  private final OperatorDatabase operatorDatabase;
  private final Map<
          Class<? extends GraphQLDirectiveContainer>,
          FocusedDirectiveWiringProvider<?, ? extends GraphQLDirectiveContainer>>
      graphQLNodeTypeToFocusedDirectiveWiringProvider;
  private final Logger logger =
      LoggerFactory.getLogger(GraphQLSchemaDirectiveWiringFactoryImpl.class);

  public GraphQLSchemaDirectiveWiringFactoryImpl(OperatorDatabase operatorDatabase) {
    this.operatorDatabase = operatorDatabase;
    this.graphQLNodeTypeToFocusedDirectiveWiringProvider = new ConcurrentHashMap<>();
    //    CustomScalarFieldDefDirectiveWiringProvider customScalarFieldDefDirectiveWiringProvider =
    //        createCustomScalarFieldDefDirectiveWiringProvider();
    //    putFocusedDirectiveWiringProviderInMap(
    //        GraphQLFieldDefinition.class, customScalarFieldDefDirectiveWiringProvider);
    SearchableFieldDefinitionDirectiveWiringProvider
        searchableFieldDefinitionDirectiveWiringProvider =
            createSearchableFieldDefinitionDirectiveWiringProviderUsingOperatorDatabase(
                operatorDatabase);
    putFocusedDirectiveWiringProviderInMap(
        GraphQLFieldDefinition.class, searchableFieldDefinitionDirectiveWiringProvider);
  }

  private SearchableFieldDefinitionDirectiveWiringProvider
      createSearchableFieldDefinitionDirectiveWiringProviderUsingOperatorDatabase(
          OperatorDatabase operatorDatabase) {
    Set<GraphQLArgumentOperator> graphQLArgumentOperators =
        GraphQLArgumentOperatorFactory.newInstanceWithOperatorDatabase(operatorDatabase)
            .createGraphQLArgumentOperatorSetFromOperatorDatabase();
    logGraphQLArgumentOperatorsSet(graphQLArgumentOperators);
    Map<String, SearchableFieldDirectiveWiring> graphQLTypeNameToSearchableDirectiveWiringsMap =
        SearchableFieldDirectiveWiringFactory.newInstanceUsingGraphQLArgumentOperatorSet(
                graphQLArgumentOperators)
            .createGraphQLTypeNameToSearchableFieldDirectiveWiringsMap();
    return SearchableFieldDefinitionDirectiveWiringProviderImpl.builder()
        .graphQLTypeNameToSearchableFieldDirectiveWirings(
            graphQLTypeNameToSearchableDirectiveWiringsMap)
        .build();
  }

  private CustomScalarFieldDefDirectiveWiringProvider
      createCustomScalarFieldDefDirectiveWiringProvider() {
    return CustomScalarFieldDefDirectiveWiringProvider.newInstance();
  }

  private <X extends GraphQLDirectiveContainer> void putFocusedDirectiveWiringProviderInMap(
      Class<X> directiveContainerType,
      FocusedDirectiveWiringProvider<? extends FocusedDirectiveWiring<X>, X>
          focusedDirectiveWiringProvider) {
    graphQLNodeTypeToFocusedDirectiveWiringProvider.put(
        directiveContainerType, focusedDirectiveWiringProvider);
  }

  private void logGraphQLArgumentOperatorsSet(
      Set<GraphQLArgumentOperator> graphQLArgumentOperators) {
    logger.info(
        String.format(
            "graphql_argument_operators: [\n\t\t %s \n]",
            graphQLArgumentOperators.stream()
                .sorted(Comparator.comparing(GraphQLArgumentOperator::argumentName))
                .map(
                    graphQLArgumentOperator ->
                        String.join(
                            ": ",
                            graphQLArgumentOperator.argumentName(),
                            graphQLArgumentOperator.toString()))
                .collect(Collectors.joining(",\n\t\t"))));
  }

  @Override
  public OperatorDatabase operatorDatabase() {
    return operatorDatabase;
  }

  @Override
  public SchemaDirectiveWiring getSchemaDirectiveWiring(
      SchemaDirectiveWiringEnvironment environment) {
    return TypeMatcher.whenTypeOf(environment.getElement())
        .is(GraphQLFieldDefinition.class)
        .thenApply(
            graphQLFieldDefinition ->
                graphQLNodeTypeToFocusedDirectiveWiringProvider
                    .get(GraphQLFieldDefinition.class)
                    .getSchemaDirectiveWiring(environment))
        .orElse(NO_MATCHING_DIRECTIVE_WIRING_RESULT);
  }

  @Override
  public boolean providesSchemaDirectiveWiring(SchemaDirectiveWiringEnvironment environment) {
    return TypeMatcher.whenTypeOf(environment.getElement())
        .is(GraphQLFieldDefinition.class)
        .thenApply(
            graphQLFieldDefinition ->
                graphQLNodeTypeToFocusedDirectiveWiringProvider
                    .get(GraphQLFieldDefinition.class)
                    .providesSchemaDirectiveWiring(environment))
        .orElse(Boolean.FALSE);
  }
}
