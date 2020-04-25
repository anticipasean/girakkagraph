package io.github.anticipasean.girakkagraph.protocol.graphql.command.schema;

import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.setup.GraphQlExecutableSchemaGenerated;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.setup.SetupGraphQlSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.immutables.value.Value;

@Value.Immutable
public interface GenerateGraphQlExecutableSchema
    extends SetupGraphQlSchema<GraphQlExecutableSchemaGenerated> {
  TypeDefinitionRegistry typeDefinitionRegistry();

  RuntimeWiring runtimeWiring();
}
