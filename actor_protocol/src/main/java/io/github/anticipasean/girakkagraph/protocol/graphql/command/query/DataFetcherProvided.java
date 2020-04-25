package io.github.anticipasean.girakkagraph.protocol.graphql.command.query;

import akka.NotUsed;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetcher;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import org.immutables.value.Value;

@Value.Immutable
public interface DataFetcherProvided extends GraphQlQueryProtocol<NotUsed> {
  Optional<Throwable> errorOccurred();

  Optional<DataFetcher<CompletionStage<DataFetcherResult<?>>>> dataFetcher();
}
