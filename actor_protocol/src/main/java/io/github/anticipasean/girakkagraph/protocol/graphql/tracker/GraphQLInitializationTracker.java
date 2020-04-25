package io.github.anticipasean.girakkagraph.protocol.graphql.tracker;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.receptionist.Receptionist;
import akka.japi.Pair;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.command.lookup.shareable.LookUpShareableResultsFound;
import io.github.anticipasean.girakkagraph.protocol.base.command.lookup.shareable.LookUpShareables;
import io.github.anticipasean.girakkagraph.protocol.base.command.lookup.shareable.LookUpShareablesImpl;
import io.github.anticipasean.girakkagraph.protocol.base.command.receptionist.RegisteredActorFound;
import io.github.anticipasean.girakkagraph.protocol.base.command.receptionist.RegisteredActorFoundImpl;
import io.github.anticipasean.girakkagraph.protocol.base.util.container.ActorRefValueContainer;
import io.github.anticipasean.girakkagraph.protocol.base.util.lookup.ShareableQueryImpl;
import io.github.anticipasean.girakkagraph.protocol.base.util.receptionist.ReceptionistInteractive;
import io.github.anticipasean.girakkagraph.protocol.base.util.shareable.Shareable;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.RegistrableImpl;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.GraphQlDependenciesReceived;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.TakeGraphQlDependencies;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.TakeGraphQlDependenciesImpl;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.query.GraphQlQueryProtocol;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.GraphQlSchemaProtocol;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.setup.GraphQlSchemaAndWiringInitialized;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.setup.InitializeGraphQlSchemaAndWiring;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.setup.InitializeGraphQlSchemaAndWiringImpl;
import io.github.anticipasean.girakkagraph.protocol.graphql.dependencies.GraphQLDependencies;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GraphQLInitializationTracker {

  public static Behavior<Command> startInitialization() {
    return Behaviors.setup(
        context -> {
          return getLookUpShareableServiceRef(context);
        });
  }

  private static Behavior<Command> getLookUpShareableServiceRef(ActorContext<Command> context) {
    submitSearchForListingForRegisteredCommandTypeAndIdInContext(
        LookUpShareables.class, "lookUpShareables", context);
    return Behaviors.receive(Command.class)
        .onMessage(
            RegisteredActorFound.class,
            cmd -> onRegisteredActorFoundForLookUpShareables(cmd, context))
        .build();
  }

  private static ActorRef<Receptionist.Listing>
      submitSearchForListingForRegisteredCommandTypeAndIdInContext(
          Class<?> registeredClass, String id, ActorContext<Command> context) {
    context
        .getLog()
        .info(
            "submitting search request to receptionist for [ "
                + Stream.of(
                        Pair.create("registeredClass", registeredClass.getName()),
                        Pair.create("id", id))
                    .map(pair -> String.join(": ", pair.first(), pair.second()))
                    .collect(Collectors.joining(","))
                + " ]");
    ActorRef<Receptionist.Listing> listingActorRef =
        ReceptionistInteractive.submitSearchForRegistrableInContextTranslatingListing(
            context,
            RegistrableImpl.of(registeredClass),
            (actorRefValCont, throwable) -> {
              RegisteredActorFoundImpl.Builder responseBuilder =
                  RegisteredActorFoundImpl.builder()
                      .registrableForActorInSearch(RegistrableImpl.of(registeredClass));
              if (throwable != null) {
                responseBuilder.errorOccurred(throwable);
              } else {
                responseBuilder.actorFoundMaybe(actorRefValCont);
              }
              return responseBuilder.build();
            });
    return listingActorRef;
  }

  private static Behavior<Command> onRegisteredActorFoundForLookUpShareables(
      RegisteredActorFound cmd, ActorContext<Command> context) {
    if (cmd.errorOccurred().isPresent()) {
      return errorOccurredOnRegisteredActorFound(cmd, context);
    }
    if (actorActuallyFoundAndMatchesExpectedRegisteredCommandType(cmd, LookUpShareables.class)) {
      ActorRefValueContainer<?> actorRefValueContainer = cmd.actorFoundMaybe().get();
      @SuppressWarnings("unchecked")
      ActorRef<LookUpShareables> actorRef =
          (ActorRef<LookUpShareables>) actorRefValueContainer.value();
      actorRef.tell(
          LookUpShareablesImpl.builder()
              .addLookUpShareableQuery(
                  ShareableQueryImpl.of(
                      "graphQLDependencies", AtomicReference.class, GraphQLDependencies.class))
              .replyTo(context.getSelf().narrow())
              .build());
      return Behaviors.receive(Command.class)
          .onMessage(
              LookUpShareableResultsFound.class,
              lookUpShareableResultsFound ->
                  onLookUpShareableResultsFound(context, lookUpShareableResultsFound))
          .build();
    }
    context
        .getLog()
        .error(
            "did not retrieve LookUpShareables actor ref",
            new NoSuchElementException("actor ref not LookShareables ref"));
    return Behaviors.stopped();
  }

  private static <R> boolean actorActuallyFoundAndMatchesExpectedRegisteredCommandType(
      RegisteredActorFound cmd, Class<R> expectedRegisteredCommandType) {
    return cmd.actorFoundMaybe().isPresent()
        && cmd.actorFoundMaybe()
            .get()
            .type()
            .isAssignableFrom(cmd.registrableForActorInSearch().protocolMessageType())
        && cmd.registrableForActorInSearch()
            .protocolMessageType()
            .isAssignableFrom(expectedRegisteredCommandType);
  }

  private static Behavior<Command> errorOccurredOnRegisteredActorFound(
      RegisteredActorFound cmd, ActorContext<Command> context) {
    context
        .getLog()
        .error(
            "an error occurred when retrieving registered actor for "
                + cmd.registrableForActorInSearch(),
            cmd.errorOccurred().get());
    return Behaviors.stopped();
  }

  private static Behavior<Command> onLookUpShareableResultsFound(
      ActorContext<Command> context, LookUpShareableResultsFound command) {
    context.getLog().info("look_up_shareable_results_found: " + command);
    Optional<Shareable> shareableMaybe = command.shareablesFound().stream().findAny();
    if (shareableMaybe.isPresent()) {
      Shareable shareable = shareableMaybe.get();
      if (shareable.name().equals("graphQLDependencies")
          && shareable.valueContainerType().isAssignableFrom(AtomicReference.class)
          && shareable.javaClass().isAssignableFrom(GraphQLDependencies.class)) {
        context.getLog().info("obtained graphQLDependencies necessary for graphql initialization");
        AtomicReference<?> holder = (AtomicReference<?>) shareable.valueContainer().value();
        GraphQLDependencies graphQLDependencies = (GraphQLDependencies) holder.get();
        return initializeGraphQLQueryService(context, graphQLDependencies);
      }
      return Behaviors.same();
    } else {
      context
          .getLog()
          .error(
              "did not receive expected shareable for this object: graphQLDependencies",
              new IllegalArgumentException(
                  "shareable received does not match the expected type: [ expected: graphQLDependencies, received: "
                      + shareableMaybe
                      + " ]"));
      return Behaviors.stopped();
    }
  }

  private static Behavior<Command> initializeGraphQLQueryService(
      ActorContext<Command> context, GraphQLDependencies graphQLDependencies) {
    ActorRef<Receptionist.Listing> receptionistTranslatorForQueryService =
        submitSearchForListingForRegisteredCommandTypeAndIdInContext(
            GraphQlQueryProtocol.class, "graphQLQueryService", context);
    return Behaviors.receive(Command.class)
        .onMessage(
            RegisteredActorFound.class,
            registeredActorFound ->
                registeredActorFound
                    .registrableForActorInSearch()
                    .protocolMessageType()
                    .isAssignableFrom(GraphQlQueryProtocol.class),
            registeredActorFound ->
                onGraphQLQueryServiceRefFound(registeredActorFound, context, graphQLDependencies))
        .onMessage(
            GraphQlDependenciesReceived.class,
            graphQLDependenciesReceived ->
                onGraphQLDependenciesReceived(
                    graphQLDependenciesReceived,
                    context,
                    () -> initializeGraphQLSchemaService(context, graphQLDependencies)))
        .build();
  }

  private static Behavior<Command> initializeGraphQLSchemaService(
      ActorContext<Command> context, GraphQLDependencies graphQLDependencies) {
    ActorRef<Receptionist.Listing> receptionistTranslatorForSchemaService =
        submitSearchForListingForRegisteredCommandTypeAndIdInContext(
            GraphQlSchemaProtocol.class, "graphQLSchemaService", context);
    return Behaviors.receive(Command.class)
        .onMessage(
            RegisteredActorFound.class,
            registeredActorFound ->
                registeredActorFound
                    .registrableForActorInSearch()
                    .protocolMessageType()
                    .isAssignableFrom(GraphQlSchemaProtocol.class),
            registeredActorFound ->
                onGraphQLSchemaServiceRefFound(registeredActorFound, context, graphQLDependencies))
        .onMessage(
            GraphQlDependenciesReceived.class,
            graphQLDependenciesReceived ->
                onGraphQLDependenciesReceived(
                    graphQLDependenciesReceived,
                    context,
                    () -> initializeGraphQLSchemaAndWiring(context)))
        .build();
  }

  private static <R>
      BiFunction<RegisteredActorFound, ActorContext<Command>, ActorRef<R>> onServiceRefFoundFunc(
          Class<R> expectedRegisteredCommandType) {
    return (registeredActorFound, context) -> {
      context.getLog().info("registered_actor_found received: " + registeredActorFound);
      if (registeredActorFound.actorFoundMaybe().isPresent()) {
        ActorRefValueContainer<?> actorRefValueContainer =
            registeredActorFound.actorFoundMaybe().get();
        if (actorRefValueContainer.type().isAssignableFrom(expectedRegisteredCommandType)) {
          @SuppressWarnings("unchecked")
          ActorRef<R> actorRef = (ActorRef<R>) actorRefValueContainer.value();
          return actorRef;
        } else {
          String message =
              String.format(
                  "actor ref value container found %s is not for expected registered command type: %s",
                  actorRefValueContainer, expectedRegisteredCommandType);
          context.getLog().error(message, new NoSuchElementException(message));
          return null;
        }
      } else {
        String message =
            "actor ref was not found for registered command type: " + expectedRegisteredCommandType;
        context.getLog().error(message, new IllegalArgumentException(message));
        return null;
      }
    };
  }

  private static Behavior<Command> onGraphQLSchemaServiceRefFound(
      RegisteredActorFound registeredActorFound,
      ActorContext<Command> context,
      GraphQLDependencies graphQLDependencies) {
    context.getLog().info("registered_actor_found received: " + registeredActorFound);
    ActorRef<Command> schemaCommandActorRef =
        onServiceRefFoundFunc(GraphQlSchemaProtocol.class)
            .apply(registeredActorFound, context)
            .unsafeUpcast();
    return schemaCommandActorRef != null
        ? sendGraphQLDependenciesIfActorRefFound(graphQLDependencies)
            .apply(schemaCommandActorRef.narrow(), context)
        : Behaviors.stopped();
  }

  private static Behavior<Command> onGraphQLQueryServiceRefFound(
      RegisteredActorFound registeredActorFound,
      ActorContext<Command> context,
      GraphQLDependencies graphQLDependencies) {
    context.getLog().info("registered_actor_found received: " + registeredActorFound);
    ActorRef<Command> queryCommandActorRef =
        onServiceRefFoundFunc(GraphQlQueryProtocol.class)
            .apply(registeredActorFound, context)
            .unsafeUpcast();
    return queryCommandActorRef != null
        ? sendGraphQLDependenciesIfActorRefFound(graphQLDependencies)
            .apply(queryCommandActorRef.narrow(), context)
        : Behaviors.stopped();
  }

  private static BiFunction<
          ActorRef<TakeGraphQlDependencies>, ActorContext<Command>, Behavior<Command>>
      sendGraphQLDependenciesIfActorRefFound(GraphQLDependencies graphQLDependencies) {
    return (takeGraphQLDependenciesActorRef, context) -> {
      takeGraphQLDependenciesActorRef.tell(
          TakeGraphQlDependenciesImpl.builder()
              .graphQlDependencies(graphQLDependencies)
              .replyTo(context.getSelf().narrow())
              .build());
      return Behaviors.same();
    };
  }

  private static Behavior<Command> onGraphQLDependenciesReceived(
      GraphQlDependenciesReceived graphQLDependenciesReceived,
      ActorContext<Command> context,
      Supplier<Behavior<Command>> onSuccess) {
    if (graphQLDependenciesReceived.errorOccurred().isPresent()) {
      String message = "an error occurred when receiving the graphql dependencies";
      context.getLog().error(message, graphQLDependenciesReceived.errorOccurred().get());
      return Behaviors.stopped();
    }
    return onSuccess.get();
  }

  private static Behavior<Command> initializeGraphQLSchemaAndWiring(ActorContext<Command> context) {
    ActorRef<Receptionist.Listing> listingActorRef =
        submitSearchForListingForRegisteredCommandTypeAndIdInContext(
            InitializeGraphQlSchemaAndWiring.class, "initializeGraphQLSchemaAndWiring", context);
    return Behaviors.receive(Command.class)
        .onMessage(
            RegisteredActorFound.class,
            registeredActorFound ->
                onInitializeGraphQLSchemaAndWiringActorFound(registeredActorFound, context))
        .onMessage(
            GraphQlSchemaAndWiringInitialized.class,
            graphQLSchemaAndWiringInitialized ->
                onGraphQLSchemaAndWiringInitialized(graphQLSchemaAndWiringInitialized, context))
        .build();
  }

  private static Behavior<Command> onGraphQLSchemaAndWiringInitialized(
      GraphQlSchemaAndWiringInitialized graphQLSchemaAndWiringInitialized,
      ActorContext<Command> context) {
    if (graphQLSchemaAndWiringInitialized.errorOccurred().isPresent()) {
      String message = "an error occurred when initializing graphql schema and wiring";
      context.getLog().error(message, graphQLSchemaAndWiringInitialized.errorOccurred().get());
      return Behaviors.stopped();
    }
    context.getLog().info("graphql schema and wiring successfully initialized");
    return Behaviors.stopped();
  }

  private static Behavior<Command> onInitializeGraphQLSchemaAndWiringActorFound(
      RegisteredActorFound registeredActorFound, ActorContext<Command> context) {
    context.getLog().info("registered_actor_found received: " + registeredActorFound);
    ActorRef<InitializeGraphQlSchemaAndWiring> initializeGraphQLSchemaAndWiringActorRef =
        onServiceRefFoundFunc(InitializeGraphQlSchemaAndWiring.class)
            .apply(registeredActorFound, context);
    if (initializeGraphQLSchemaAndWiringActorRef != null) {
      InitializeGraphQlSchemaAndWiring initializeGraphQLSchemaAndWiring =
          InitializeGraphQlSchemaAndWiringImpl.builder()
              .replyTo(context.getSelf().narrow())
              .build();
      initializeGraphQLSchemaAndWiringActorRef.tell(initializeGraphQLSchemaAndWiring);
      return Behaviors.same();
    }
    String message = "unable to find actor who initializes graphql schema and wiring";
    context.getLog().error(message, new NoSuchElementException(message));
    return Behaviors.stopped();
  }
}
