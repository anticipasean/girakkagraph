package io.github.anticipasean.girakkagraph.protocol.graphql.domain.directive.wiring.field;

import graphql.Scalars;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLNamedType;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Style;

@Immutable
@Style(
    typeImmutable = "*Impl",
    overshadowImplementation = true,
    depluralize = true,
    depluralizeDictionary = {
      "compatibleGraphQLArgumentOperator:compatibleGraphQLArgumentOperators"
    })
public abstract class BaseSearchableFieldDirectiveWiring implements SearchableFieldDirectiveWiring {

  @Override
  public Predicate<GraphQLFieldDefinition> shouldApplyDirectiveToWiringElement() {
    return graphQLFieldDefinition -> graphQLFieldDefinition.getType() instanceof GraphQLNamedType
        && ((GraphQLNamedType) graphQLFieldDefinition.getType()).getName().equals(graphQLTypeName());
  }

  @Override
  public GraphQLFieldDefinition onWiringElementEncountered(GraphQLFieldDefinition fieldDefinition) {
    Optional<GraphQLDirective> searchFilterDirectiveDefinedOpt =
        getSearchableDirectiveOnFieldDefinitionIfPresent(fieldDefinition);
    GraphQLFieldDefinition updatedGraphQLFieldDefinition = null;
    if (alreadyHasSearchFilterDirectiveDefined(searchFilterDirectiveDefinedOpt)) {
      if (searchableDirectiveOnFieldDefinitionDeactivated(
          searchFilterDirectiveDefinedOpt.get())) {
        return fieldDefinition;
      }
      updatedGraphQLFieldDefinition = fieldDefinition;
    } else {
      updatedGraphQLFieldDefinition = addSearchableDirectiveToFieldDefinition(fieldDefinition);
    }
    return addSearchArgumentsToFieldDefinitionIfMissing(updatedGraphQLFieldDefinition);
  }

  private GraphQLFieldDefinition addSearchArgumentsToFieldDefinitionIfMissing(
      GraphQLFieldDefinition updatedGraphQLFieldDefinition) {
    Set<String> currentArgumentNamesSetForFieldDefinition =
        updatedGraphQLFieldDefinition.getArguments().stream()
            .map(GraphQLArgument::getName)
            .collect(Collectors.toSet());
    GraphQLInputType searchFilterType =
        Optional.of(updatedGraphQLFieldDefinition.getType())
            .filter(graphQLType -> graphQLType instanceof GraphQLInputType)
            .map(graphQLType -> (GraphQLInputType) graphQLType)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "argument type for GraphQLArgument must be a GraphQLInputType"));
    List<GraphQLArgument> filterArgs =
        compatibleGraphQLArgumentNameToGraphQLArgumentOperator().entrySet().stream()
            .filter(entry -> !currentArgumentNamesSetForFieldDefinition.contains(entry.getKey()))
            .map(entry -> graphQLArgumentCreator().apply(searchFilterType, entry.getValue()))
            .collect(Collectors.toList());
    List<GraphQLArgument> updatedArgsList =
        Stream.concat(updatedGraphQLFieldDefinition.getArguments().stream(), filterArgs.stream())
            .collect(Collectors.toList());
    return updatedGraphQLFieldDefinition.transform(builder -> builder.arguments(updatedArgsList));
  }

  private GraphQLFieldDefinition addSearchableDirectiveToFieldDefinition(
      GraphQLFieldDefinition fieldDefinition) {
    return fieldDefinition.transform(builder -> builder.withDirective(getDirective()));
  }

  private boolean searchableDirectiveOnFieldDefinitionDeactivated(
      GraphQLDirective graphQLDirective) {
    return Optional.ofNullable(graphQLDirective.getArgument(argumentForSearchDirective().getName()))
        .filter(graphQLArgument -> graphQLArgument.getType().equals(Scalars.GraphQLBoolean))
        .map(graphQLArgument -> (Boolean) graphQLArgument.getValue())
        .orElse(Boolean.FALSE);
  }

  private boolean alreadyHasSearchFilterDirectiveDefined(
      Optional<GraphQLDirective> searchFilterDirectiveDefinedOpt) {
    return searchFilterDirectiveDefinedOpt.isPresent();
  }

  private Optional<GraphQLDirective> getSearchableDirectiveOnFieldDefinitionIfPresent(
      GraphQLFieldDefinition fieldDefinition) {
    Optional<GraphQLDirective> searchFilterDirectiveOpt =
        Optional.ofNullable(fieldDefinition.getDirective(getSearchableDirectiveGraphQLName()));
    return searchFilterDirectiveOpt;
  }
}
