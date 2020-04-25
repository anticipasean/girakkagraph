package io.github.anticipasean.girakkagraph.protocol.graphql;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.protocol.RegistrableProtocolActor;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.BehaviorCreator;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SpawnedContext;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.Subordinate;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SubordinateSpawner;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.ActorRefRegistrations;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.GraphQlProtocol;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.query.GraphQlQueryProtocol;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.GraphQlSchemaProtocol;
import io.github.anticipasean.girakkagraph.protocol.graphql.service.query.GraphQLQueryServiceActor;
import io.github.anticipasean.girakkagraph.protocol.graphql.service.schema.GraphQLSchemaServiceActor;
import java.util.Optional;

public class GraphQLProtocolActor extends RegistrableProtocolActor<GraphQlProtocol> {

  private GraphQLProtocolActor(SpawnedContext<Command, GraphQlProtocol> spawnedContext) {
    super(spawnedContext);
  }

  public static Behavior<Command> create(ActorRef<Command> parentActorRef) {
    return BehaviorCreator.create(parentActorRef, GraphQlProtocol.class, GraphQLProtocolActor::new);
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilderWithDefaultCommandHandlers()
        .onMessage(GraphQlSchemaProtocol.class, this::onGraphQLSchemaCommand)
        .onMessage(GraphQlQueryProtocol.class, this::onGraphQLQueryCommand)
        .build();
  }

  private Behavior<Command> onGraphQLSchemaCommand(GraphQlSchemaProtocol command) {
    context.getLog().info("graphql_schema_command received: " + command);
    Optional<ActorRef<Command>> schemaServiceRefMaybe =
        subordinateByCommandTypeHandled(GraphQlSchemaProtocol.class)
            .map(Subordinate::subordinateRef);
    if (schemaServiceRefMaybe.isPresent()) {
      schemaServiceRefMaybe.get().tell(command);
    } else {
      context
          .getLog()
          .error(
              "the graphql schema service does not have a reference in the "
                  + "graphql protocol service map; it may not have been initialized properly.");
      return Behaviors.stopped();
    }
    return Behaviors.same();
  }

  private Behavior<Command> onGraphQLQueryCommand(GraphQlQueryProtocol command) {
    context.getLog().info("graphql_query_command received: " + command);
    Optional<ActorRef<Command>> queryServiceRefMaybe =
        subordinateByCommandTypeHandled(GraphQlSchemaProtocol.class)
            .map(Subordinate::subordinateRef);
    if (queryServiceRefMaybe.isPresent()) {
      queryServiceRefMaybe.get().tell(command);
    } else {
      context
          .getLog()
          .error(
              "the graphql query service does not have a reference in the "
                  + "graphql protocol service map; it may not have been initialized properly.");
      return Behaviors.stopped();
    }
    return Behaviors.same();
  }

  @Override
  protected SubordinateSpawner<Command> spawnSubordinatesOnContextCreation() {
    return newSubordinateSpawner()
        .addSpawningBehavior(GraphQlQueryProtocol.class, GraphQLQueryServiceActor::create)
        .addSpawningBehavior(GraphQlSchemaProtocol.class, GraphQLSchemaServiceActor::create);
  }

  @Override
  public ActorRefRegistrations<GraphQlProtocol> registrations() {
    return newRegistrationsBuilder().build();
  }
}
