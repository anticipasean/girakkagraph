package io.github.anticipasean.girakkagraph.springboot.web.controller;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import akka.actor.typed.receptionist.Receptionist;
import akka.japi.function.Creator;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.Registrable;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.query.ExecuteGraphQlInvocationImpl;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.query.GraphQlInvocationExecuted;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.query.GraphQlQueryProtocol;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.spring.web.reactive.GraphQLInvocation;
import graphql.spring.web.reactive.GraphQLInvocationData;
import org.dataloader.DataLoaderRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
/**
 * Copied from default {@link graphql.spring.web.reactive.components.DefaultGraphQLInvocation} and
 * modified for actor and stream implementation
 */
@Component
@Primary
public class AkkaGraphQLInvocation implements GraphQLInvocation {
  private final ActorSystem<Command> actorSystem;
  private final Registrable<GraphQlQueryProtocol> graphQLQueryCommandRegistrable;
  private final DataLoaderRegistry dataLoaderRegistry;
  private final AtomicReference<ActorRef<GraphQlQueryProtocol>> graphQLQueryServiceActorRefHolder;
  private final Logger logger = LoggerFactory.getLogger(AkkaGraphQLInvocation.class);

  public AkkaGraphQLInvocation(
      ActorSystem<Command> actorSystem, Registrable<GraphQlQueryProtocol> graphQLQueryCommandRegistrable) {
    this.actorSystem = actorSystem;
    this.graphQLQueryCommandRegistrable = graphQLQueryCommandRegistrable;
    this.dataLoaderRegistry = null;
    this.graphQLQueryServiceActorRefHolder = new AtomicReference<>();
  }

  @Override
  public Mono<ExecutionResult> invoke(
      GraphQLInvocationData invocationData, ServerWebExchange serverWebExchange) {
    logger.info("received graphql_invocation: " + invocationData);
    ExecutionInput.Builder executionInputBuilder =
        ExecutionInput.newExecutionInput()
            .query(invocationData.getQuery())
            .operationName(invocationData.getOperationName())
            .variables(invocationData.getVariables());
    if (dataLoaderRegistry != null) {
      executionInputBuilder.dataLoaderRegistry(dataLoaderRegistry);
    }
    ExecutionInput executionInput = executionInputBuilder.build();
    return Mono.just(executionInput)
        .flatMap(input -> Mono.fromCompletionStage(executeAsyncGraphQLActorInvocation(input)));
  }

  private CompletionStage<ExecutionResult> executeAsyncGraphQLActorInvocation(
      ExecutionInput executionInput) {
    return Source.lazilyAsync(executionInputGraphQLActorInvocationCreator(executionInput))
        .map(GraphQlInvocationExecuted::executionResult)
        .runWith(Sink.head(), Materializer.matFromSystem(actorSystem));
  }

  private Creator<CompletionStage<GraphQlInvocationExecuted>>
      executionInputGraphQLActorInvocationCreator(ExecutionInput executionInput) {
    return () -> {
      logger.info("asking graphql query service for executionResponse");
      return AskPattern.ask(
          getGraphQLQueryServiceRef(),
          ref ->
              ExecuteGraphQlInvocationImpl.builder()
                  .commandId(UUID.randomUUID())
                  .replyTo(ref)
                  .executionInput(executionInput)
                  .build(),
          Duration.ofSeconds(25),
          actorSystem.scheduler());
    };
  }

  private CompletionStage<Receptionist.Listing>
      askActorSystemReceptionistForGraphQLQueryServiceActorRef() {
    return AskPattern.ask(
        actorSystem.<Receptionist.Listing>receptionist(),
        ref -> Receptionist.<GraphQlQueryProtocol>find(graphQLQueryCommandRegistrable.serviceKey(), ref.narrow()),
        Duration.ofSeconds(10),
        actorSystem.scheduler());
  }

  private BiFunction<Receptionist.Listing, Throwable, ActorRef<GraphQlQueryProtocol>>
      handleGraphQLQueryServiceActorRefListingFuture() {
    return (listing, throwable) -> {
      if (throwable != null) {
        throw new RuntimeException(throwable);
      }
      Receptionist.Listing checkedListing =
          Optional.ofNullable(listing)
              .orElseThrow(
                  () ->
                      new NullPointerException(
                          "listing obtained when retrieving graphql query service actor ref is null"));
      if (!checkedListing.isForKey(graphQLQueryCommandRegistrable.serviceKey())) {
        throw new RuntimeException(
            String.format(
                "listing obtained for graphql query actor service ref [ %s ] is not for service key [ %s ]",
                checkedListing, graphQLQueryCommandRegistrable.serviceKey()));
      }
      ActorRef<GraphQlQueryProtocol> graphQLQueryServiceRef =
          checkedListing.getServiceInstances(graphQLQueryCommandRegistrable.serviceKey()).stream()
              .findFirst()
              .orElseThrow(
                  () ->
                      new RuntimeException(
                          "service listing set is empty; unable to get graphql query service actor ref"));
      return graphQLQueryServiceRef;
    };
  }

  private void updateGraphQLQueryServiceRefHolderIfServiceRefReceived(
      CompletableFuture<ActorRef<GraphQlQueryProtocol>> graphQLQueryServiceRefFuture) {
    try {
      // TODO: place magic number timeouts in config
      ActorRef<GraphQlQueryProtocol> graphQLQueryServiceRef =
          graphQLQueryServiceRefFuture.get(10, TimeUnit.SECONDS);
      graphQLQueryServiceActorRefHolder.set(graphQLQueryServiceRef);
    } catch (InterruptedException e) {
      logger.error(
          "unable to complete async graphql actor invocation due to interruption when getting graphql query service actor ref: ",
          e);
    } catch (ExecutionException e) {
      logger.error(
          "unable to complete async graphql actor invocation due to failure to get graphql query service actor ref: ",
          e);
    } catch (TimeoutException e) {
      logger.error(
          "unable to complete async graphql actor invocation due to a timeout when getting graphql query service actor ref: ",
          e);
    }
  }

  private ActorRef<GraphQlQueryProtocol> getGraphQLQueryServiceRef() {
    if (graphQLQueryServiceActorRefHolder.get() != null) {
      return graphQLQueryServiceActorRefHolder.get();
    }
    CompletionStage<Receptionist.Listing> graphQLQueryServiceRefListingFuture =
        askActorSystemReceptionistForGraphQLQueryServiceActorRef();
    CompletableFuture<ActorRef<GraphQlQueryProtocol>> graphQLQueryServiceRefFuture =
        graphQLQueryServiceRefListingFuture
            .handle(handleGraphQLQueryServiceActorRefListingFuture())
            .toCompletableFuture();
    updateGraphQLQueryServiceRefHolderIfServiceRefReceived(graphQLQueryServiceRefFuture);
    // TODO: If error occurs, inform actor system that service ref has not been provided and
    // potentially restart service
    return graphQLQueryServiceActorRefHolder.get();
  }
}
