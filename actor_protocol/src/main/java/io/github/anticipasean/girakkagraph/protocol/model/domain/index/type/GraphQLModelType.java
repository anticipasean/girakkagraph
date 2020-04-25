package io.github.anticipasean.girakkagraph.protocol.model.domain.index.type;

import io.github.anticipasean.girakkagraph.protocol.model.domain.index.ModelType;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import java.util.HashSet;
import java.util.Set;
import org.immutables.value.Value;

interface GraphQLModelType extends ModelType {

  @Value.Derived
  default String graphQlObjectTypeName() {
    return graphQlObjectType().getName();
  }

  //  @Value.Derived
  //  default GraphQLOutputType graphQlOutputType() {
  //    return graphQlObjectTypeDefinition().getImplements().stream()
  //        .filter(type -> type instanceof GraphQLOutputType)
  //        .map(GraphQLOutputType.class::cast)
  //        .findFirst()
  //        .orElseThrow(
  //            () ->
  //                new IllegalArgumentException(
  //                    "graphql object type definition does not have a graphqloutputtype: "
  //                        + graphQlObjectTypeDefinition()));
  //  }

  GraphQLObjectType graphQlObjectType();

  @Value.Derived
  default Set<GraphQLFieldDefinition> graphQlAttributeFieldDefinitions() {
    return new HashSet<>(graphQlObjectType().getFieldDefinitions());
  }
}
