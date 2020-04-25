package io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.setup;

import graphql.schema.GraphQLSchema;
import org.immutables.value.Value;

@Value.Immutable
public interface GenerateTypeDefinitionRegistry
    extends SetupGraphQlSchema<TypeDefinitionRegistryGenerated> {
  GraphQLSchema graphQLSchema();
}
