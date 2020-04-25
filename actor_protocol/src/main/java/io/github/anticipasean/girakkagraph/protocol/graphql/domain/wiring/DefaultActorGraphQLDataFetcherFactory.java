package io.github.anticipasean.girakkagraph.protocol.graphql.domain.wiring;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import akka.japi.Creator;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.stream.typed.javadsl.ActorFlow;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.query.DataFetcherProvided;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.query.GraphQlQueryProtocol;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.query.ProvideDataFetcher;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.query.ProvideDataFetcherImpl;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetcherFactory;
import graphql.schema.DataFetcherFactoryEnvironment;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import org.slf4j.Logger;

public class DefaultActorGraphQLDataFetcherFactory
    implements DataFetcherFactory<CompletionStage<DataFetcherResult<?>>> {
  private ActorSystem<Void> actorSystem;
  private AtomicReference<ActorRef<GraphQlQueryProtocol>> graphQLQueryServiceRef;
  private Logger logger;

  public DefaultActorGraphQLDataFetcherFactory(
      ActorSystem<Void> actorSystem,
      AtomicReference<ActorRef<GraphQlQueryProtocol>> graphQLQueryServiceRef) {
    this.actorSystem = actorSystem;
    this.graphQLQueryServiceRef = graphQLQueryServiceRef;
    this.logger = actorSystem.log();
  }

  /**
   * Returns a {@link DataFetcher}
   *
   * @param environment the environment that needs the data fetcher
   * @return a data fetcher
   */
  @Override
  public DataFetcher<CompletionStage<DataFetcherResult<?>>> get(
      DataFetcherFactoryEnvironment environment) {
    //        return getCompletionStageDataFetcherUsingAskPattern(environment);
    return new DefaultGraphQLDataFetcher(actorSystem, graphQLQueryServiceRef);
    //    return getCompletionStageDataFetcherUsingAskPatternAndNoAsync(environment);
  }

  private DataFetcher<CompletionStage<DataFetcherResult<?>>>
      getCompletionStageDataFetcherUsingAkkaStreams(DataFetcherFactoryEnvironment environment) {
    logger.info(
        "requesting data fetcher from ["
            + graphQLQueryServiceRef.get()
            + "] for data fetcher factory env: "
            + environment);
    try {
      return Source.single(
              ProvideDataFetcherImpl.builder()
                  .commandId(UUID.randomUUID())
                  .dataFetcherFactoryEnvironment(environment))
          .limit(1)
          .via(
              ActorFlow
                  .<ProvideDataFetcherImpl.Builder, ProvideDataFetcher, DataFetcherProvided>ask(
                      graphQLQueryServiceRef.get().narrow(),
                      Duration.ofSeconds(10),
                      (v1, v2) -> v1.replyTo(v2).build()))
          .map(DataFetcherProvided::dataFetcher)
          .runWith(Sink.head(), Materializer.matFromSystem(actorSystem))
          .toCompletableFuture()
          .join()
          .orElseThrow(() -> new IllegalStateException("data fetcher not provided for querying"));
    } catch (Exception e) {
      logger.error(
          "an error occurred when retrieving the data fetcher for "
              + environment.getFieldDefinition(),
          e);
    }
    throw new IllegalStateException("data fetcher not provided for querying");
  }

  private DataFetcher<CompletionStage<DataFetcherResult<?>>>
      getCompletionStageDataFetcherUsingAskPattern(DataFetcherFactoryEnvironment environment) {
    Creator<CompletionStage<DataFetcherProvided>> provideDataFetcherCommandCreator =
        () ->
            AskPattern.ask(
                graphQLQueryServiceRef.get(),
                ref ->
                    ProvideDataFetcherImpl.builder()
                        .commandId(UUID.randomUUID())
                        .dataFetcherFactoryEnvironment(environment)
                        .replyTo(ref.narrow())
                        .build(),
                Duration.ofSeconds(5),
                actorSystem.scheduler());
    BiFunction<DataFetcherProvided, Throwable, DataFetcher<CompletionStage<DataFetcherResult<?>>>>
        handleAsyncOutputDataFetcherMaybe =
            (dataFetcherProvided, throwable) -> {
              return Optional.ofNullable(dataFetcherProvided.dataFetcher())
                  .map(Optional::get)
                  .orElseThrow(() -> new RuntimeException(throwable));
            };
    CompletionStage<DataFetcher<CompletionStage<DataFetcherResult<?>>>> dataFetcherStream =
        Source.lazyCompletionStage(
                () ->
                    provideDataFetcherCommandCreator
                        .create()
                        .handleAsync(
                            handleAsyncOutputDataFetcherMaybe, actorSystem.executionContext()))
            .runWith(Sink.head(), actorSystem);
    return dataFetcherStream.toCompletableFuture().join();
  }

  private DataFetcher<CompletionStage<DataFetcherResult<?>>>
      getCompletionStageDataFetcherUsingAskPatternAndNoAsync(
          DataFetcherFactoryEnvironment environment) {
    CompletionStage<DataFetcherProvided> dataFetcherProvidedCompletionStage =
        AskPattern.ask(
            graphQLQueryServiceRef.get(),
            ref ->
                ProvideDataFetcherImpl.builder()
                    .commandId(UUID.randomUUID())
                    .dataFetcherFactoryEnvironment(environment)
                    .replyTo(ref.narrow())
                    .build(),
            Duration.ofSeconds(5),
            actorSystem.scheduler());

    return dataFetcherProvidedCompletionStage
        .thenApply(DataFetcherProvided::dataFetcher)
        .thenApply(
            completionStageDataFetcher ->
                completionStageDataFetcher.orElseThrow(
                    () -> new IllegalStateException("no datafetcher provided by query service")))
        .toCompletableFuture()
        .join();
  }
}
