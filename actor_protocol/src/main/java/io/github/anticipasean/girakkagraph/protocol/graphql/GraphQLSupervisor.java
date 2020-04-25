package io.github.anticipasean.girakkagraph.protocol.graphql;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.japi.Pair;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.supervisor.RegistrableProtocolSupervisor;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.BehaviorCreator;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SpawnedContext;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.Subordinate;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SubordinateSpawner;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.ActorRefRegistrations;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.GraphQlProtocol;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GraphQLSupervisor extends RegistrableProtocolSupervisor<GraphQlProtocol> {

  protected GraphQLSupervisor(SpawnedContext<Command, GraphQlProtocol> spawnedContext) {
    super(spawnedContext);
    context
        .getLog()
        .info(
            "graphql supervisor initialized: [ "
                + context.getSelf().path().toSerializationFormat()
                + " ]");
    context
        .getLog()
        .info(
            String.format(
                "graphql supervisor path components: [ %s ]",
                Stream.of(
                        Pair.create("path.address", context.getSelf().path().address()),
                        Pair.create("path.root", context.getSelf().path().root()),
                        Pair.create("path.name", context.getSelf().path().name()),
                        Pair.create("path.elements", context.getSelf().path().elements()))
                    .map(stringPair -> stringPair.first() + ": " + stringPair.second())
                    .collect(Collectors.joining(",\n"))));
    context.getLog().info("initializing graphql protocol");
  }

  public static Behavior<Command> create(ActorRef<Command> parentActorRef) {
    return BehaviorCreator.create(parentActorRef, GraphQlProtocol.class, GraphQLSupervisor::new);
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilderWithDefaultCommandHandlers()
        .onMessage(GraphQlProtocol.class, this::onGraphQLCommand)
        .build();
  }

  private Behavior<Command> onGraphQLCommand(GraphQlProtocol graphQLCommand) {
    context.getLog().info("graphql_command received: " + graphQLCommand);
    Optional<Subordinate<Command>> graphQLProtocolSubordinateMaybe =
        subordinateByCommandTypeHandled(GraphQlProtocol.class);
    graphQLProtocolSubordinateMaybe.ifPresent(
        subordinate -> {
          subordinate.subordinateRef().tell(graphQLCommand);
        });
    return Behaviors.same();
  }

  @Override
  protected SubordinateSpawner<Command> spawnSubordinatesOnContextCreation() {
    return newSubordinateSpawner()
        .addSpawningBehavior(GraphQlProtocol.class, GraphQLProtocolActor::create);
  }

  @Override
  public ActorRefRegistrations<GraphQlProtocol> registrations() {
    return newRegistrationsBuilder().build();
  }
}
