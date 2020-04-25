package io.github.anticipasean.girakkagraph.protocol.graphql.command.query;

import akka.NotUsed;
import graphql.execution.DataFetcherResult;
import org.immutables.value.Value;

@Value.Immutable
public interface GraphQlQueried extends GraphQlQueryProtocol<NotUsed> {
  DataFetcherResult<?> dataFetcherResult();
}
