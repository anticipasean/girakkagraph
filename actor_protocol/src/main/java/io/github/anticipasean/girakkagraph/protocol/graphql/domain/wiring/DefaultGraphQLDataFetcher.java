package io.github.anticipasean.girakkagraph.protocol.graphql.domain.wiring;

import akka.NotUsed;
import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.stream.typed.javadsl.ActorFlow;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.query.GraphQlQueried;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.query.GraphQlQueryProtocol;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.query.QueryGraphQlImpl;
import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;

public class DefaultGraphQLDataFetcher
    implements DataFetcher<CompletionStage<DataFetcherResult<?>>> {
  private final ActorSystem<Void> actorSystem;
  private final AtomicReference<ActorRef<GraphQlQueryProtocol>> refHolderForService;
  private final Logger logger;

  public DefaultGraphQLDataFetcher(
      ActorSystem<Void> actorSystem,
      AtomicReference<ActorRef<GraphQlQueryProtocol>> refHolderForService) {
    this.actorSystem = actorSystem;
    this.refHolderForService = refHolderForService;
    logger = this.actorSystem.log();
  }

  /**
   * This is called by the graphql engine to fetch the value. The {@link DataFetchingEnvironment} is
   * a composite context object that tells you all you need to know about how to fetch a data value
   * in graphql type terms.
   *
   * @param environment this is the data fetching environment which contains all the context you
   *     need to fetch a value
   * @return a value of type T. May be wrapped in a {@link DataFetcherResult}
   * @throws Exception to relieve the implementations from having to wrap checked exceptions. Any
   *     exception thrown from a {@code DataFetcher} will eventually be handled by the registered
   *     {@link DataFetcherExceptionHandler} and the related field will have a value of {@code null}
   *     in the result.
   */
  @Override
  public CompletionStage<DataFetcherResult<?>> get(DataFetchingEnvironment environment)
      throws Exception {
    return getDataFetcherResultViaAkkaStreams(environment);
  }

  private CompletionStage<DataFetcherResult<?>> getDataFetcherResultViaAkkaStreams(
      DataFetchingEnvironment environment) {
    logger.info(
        "getting datafetcherresult from "
            + refHolderForService.get()
            + " for datafetchingenvironment: "
            + environment);

    Source<GraphQlQueried, NotUsed> graphQLQueriedSource =
        Source.single(
                QueryGraphQlImpl.builder()
                    .commandId(UUID.randomUUID())
                    .dataFetchingEnvironment(environment))
            .limit(1)
            .via(
                ActorFlow.ask(
                    refHolderForService.get().narrow(),
                    Duration.ofSeconds(20),
                    (v1, v2) -> v1.replyTo(v2).build()));
    CompletionStage<DataFetcherResult<?>> dataFetcherResultCompletionStage =
        graphQLQueriedSource
            .<DataFetcherResult<?>>map(GraphQlQueried::dataFetcherResult)
            .runWith(Sink.head(), Materializer.matFromSystem(actorSystem));
    return dataFetcherResultCompletionStage;
  }
}

/*
  private CompletionStage<DataFetcherResult<?>> getDataFetcherResultViaCreator(
      DataFetchingEnvironment environment) throws Exception {
    Creator<CompletionStage<GraphQLQueried>> queryGraphQLCommandCreator =
        getQueryGraphQLCommandCreator(environment);
    CompletionStage<DataFetcherResult<?>> dataFetcherResultCompletionStage =
        queryGraphQLCommandCreator
            .create()
            .handleAsync(
                (graphQLQueried, throwable) ->
                    Optional.ofNullable(graphQLQueried.dataFetcherResult())
                        .orElseThrow(() -> new RuntimeException(throwable)));
    return dataFetcherResultCompletionStage;
  }

  private Creator<CompletionStage<GraphQLQueried>> getQueryGraphQLCommandCreator(
      DataFetchingEnvironment environment) {
    return () ->
        AskPattern.<GraphQLQueryCommand, GraphQLQueried>ask(
            refHolderForService.get(),
            ref ->
                QueryGraphQLImpl.builder()
                    .commandId(UUID.fromString(environment.getExecutionId().toString()))
                    .replyTo(ref)
                    .dataFetchingEnvironment(environment)
                    .build(),
            Duration.ofSeconds(5),
            actorSystem.scheduler());
  }
*/
