package io.github.anticipasean.girakkagraph.protocol.model.domain.index.attribute;

import com.google.common.collect.ImmutableSet;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.ModelAttribute;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLOutputType;
import java.util.Optional;
import java.util.Set;
import org.immutables.value.Value;

interface GraphQLModelAttribute extends ModelAttribute {

  @Value.Derived
  default String graphQlFieldName() {
    return graphQlFieldDefinition().getName();
  }

  @Value.Derived
  default GraphQLOutputType graphQlOutputType() {
    return graphQlFieldDefinition().getType();
  }

  GraphQLFieldDefinition graphQlFieldDefinition();

  @Value.Derived
  default Set<GraphQLArgument> graphQlArguments() {
    return ImmutableSet.copyOf(graphQlFieldDefinition().getArguments());
  }

  @Value.Derived
  default String graphQlOutputTypeName() {
    if (graphQlOutputType() instanceof GraphQLList) {
      return Optional.ofNullable((GraphQLList) graphQlOutputType())
          .map(GraphQLList::getWrappedType)
          .filter(graphQLType -> graphQLType instanceof GraphQLNamedType)
          .map(graphQLType -> ((GraphQLNamedType) graphQLType).getName())
          .orElseThrow(
              () ->
                  new IllegalStateException(
                      "graphql output type name not present for attribute with graphql field definition: "
                          + graphQlFieldDefinition()));
    } else {
      return Optional.ofNullable(graphQlOutputType())
          .filter(graphQLOutputType -> graphQLOutputType instanceof GraphQLNamedType)
          .map(graphQLOutputType -> ((GraphQLNamedType) graphQLOutputType).getName())
          .orElseThrow(
              () ->
                  new IllegalStateException(
                      "graphql output type name not present for attribute with graphql field definition: "
                          + graphQlFieldDefinition()));
    }
  }
}
