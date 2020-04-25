package io.github.anticipasean.girakkagraph.protocol.graphql;

import akka.actor.typed.ActorSystem;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import com.typesafe.config.ConfigFactory;

import java.io.File;
import org.junit.jupiter.api.Test;

public class GraphQlProtocolActorTest {
  ActorSystem<Command> actorSystem;

  @Test
  void setUp() {
    actorSystem =
        ActorSystem.create(
            GraphQLProtocolActor.create(null),
            "graphql",
            ConfigFactory.parseFile(new File("actor_protocol/src/main/resources/reference.conf")));
  }


  void tearDown() {}

  //  @Test
  //  void createGraphQLProtocol() {
  //    ActorRef<GetQueryResult> actorRef = actorSystem.narrow();
  //    CompletionStage<GetQueryResult> resultCompletionStage =
  //        AskPattern.ask(
  //            actorSystem,
  //            ar ->
  //                QueryGraphQLImpl.builder()
  //                    .commandId(UUID.randomUUID())
  //
  //                    .replyTo(ar)
  //                    .build(),
  //            Duration.ofSeconds(20),
  //            actorSystem.scheduler());
  //
  //    Assertions.assertDoesNotThrow(
  //        () -> {
  //          resultCompletionStage.toCompletableFuture().get(20L, TimeUnit.SECONDS);
  //        });
  //    actorSystem.terminate();
  //  }

  //  @Test
  //  void getGraphQLServiceKeyMap() throws InterruptedException, ExecutionException,
  // TimeoutException {
  //    ActorRef<GetGraphQLServiceKeyMap> actorRef = actorSystem.narrow();
  //    CompletionStage<TakeServiceKeyMap<GraphQLCommand>> keyMapCompletionStage =
  //        AskPattern.ask(
  //            actorSystem,
  //            ar -> {
  //              return GetGraphQLServiceKeyMapImpl.builder()
  //                  .commandId(UUID.randomUUID())
  //                  .replyTo(ar)
  //                  .build();
  //            },
  //            Duration.ofSeconds(10),
  //            actorSystem.scheduler());
  //    Assertions.assertTrue(
  //        keyMapCompletionStage
  //            .toCompletableFuture()
  //            .get(10L, TimeUnit.SECONDS)
  //            .serviceKeyMap()
  //            .containsKey(QueryGraphQL.class));
  //  }
}
