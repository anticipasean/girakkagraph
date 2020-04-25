package io.github.anticipasean.girakkagraph.protocol.graphql.domain.directive.wiring.field;

import io.github.anticipasean.girakkagraph.protocol.graphql.domain.directive.wiring.FocusedDirectiveWiringProvider;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLNamedType;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.immutables.value.Value.Immutable;

@Immutable
public interface SearchableFieldDefinitionDirectiveWiringProvider
    extends FocusedDirectiveWiringProvider<SearchableFieldDirectiveWiring, GraphQLFieldDefinition> {

  Map<String, SearchableFieldDirectiveWiring> graphQLTypeNameToSearchableFieldDirectiveWirings();

  @Override
  default Class<GraphQLFieldDefinition> wiringElementType() {
    return GraphQLFieldDefinition.class;
  }

  @Override
  default Function<GraphQLFieldDefinition, Optional<SearchableFieldDirectiveWiring>>
      retrieveFocusedDirectiveWiringForWiringElement() {
    return graphQLFieldDefinition -> {
      if (graphQLFieldDefinition.getType() instanceof GraphQLNamedType) {
        String currentFieldDefinitionTypeName =
            ((GraphQLNamedType) graphQLFieldDefinition.getType()).getName();
        if (graphQLTypeNameToSearchableFieldDirectiveWirings()
            .containsKey(currentFieldDefinitionTypeName)) {
          return Optional.of(
              graphQLTypeNameToSearchableFieldDirectiveWirings()
                  .get(currentFieldDefinitionTypeName));
        }
      }
      return Optional.empty();
    };
  }
}
