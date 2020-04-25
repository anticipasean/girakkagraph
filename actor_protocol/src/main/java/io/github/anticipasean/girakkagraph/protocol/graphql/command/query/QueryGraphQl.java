package io.github.anticipasean.girakkagraph.protocol.graphql.command.query;

import graphql.schema.DataFetchingEnvironment;
import org.immutables.value.Value;

@Value.Immutable
public interface QueryGraphQl extends GraphQlQueryProtocol<GraphQlQueried> {
  DataFetchingEnvironment dataFetchingEnvironment();
}
