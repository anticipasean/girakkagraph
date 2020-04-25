package io.github.anticipasean.girakkagraph.protocol.graphql.command.query;

import graphql.schema.DataFetcherFactoryEnvironment;
import org.immutables.value.Value;

@Value.Immutable
public interface ProvideDataFetcher extends GraphQlQueryProtocol<DataFetcherProvided> {
  DataFetcherFactoryEnvironment dataFetcherFactoryEnvironment();
}
