package io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.setup;

import akka.NotUsed;
import graphql.schema.GraphQLSchema;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
public interface GraphQlExecutableSchemaGenerated extends SetupGraphQlSchema<NotUsed> {
  Optional<GraphQLSchema> graphQlSchemaMaybe();
}
