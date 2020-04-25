package io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.setup;

import akka.NotUsed;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.GraphQlSchemaProtocol;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetcherFactory;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import org.immutables.value.Value;

@Value.Immutable
public interface DataFetcherFactoryProvided extends GraphQlSchemaProtocol<NotUsed> {
  Optional<DataFetcherFactory<CompletionStage<DataFetcherResult<?>>>> dataFetcherFactory();
}
