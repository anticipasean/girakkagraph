package io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.info;

import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.GraphQlSchemaProtocol;
import org.immutables.value.Value;

@Value.Immutable
public interface FetchGraphQlSchema extends GraphQlSchemaProtocol<GraphQlSchemaFetched> {}
