package io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.info;

import akka.NotUsed;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.GraphQlSchemaProtocol;
import graphql.schema.GraphQLSchema;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
public interface GraphQlSchemaFetched extends GraphQlSchemaProtocol<NotUsed> {
  Optional<GraphQLSchema> graphQLSchema();
}
