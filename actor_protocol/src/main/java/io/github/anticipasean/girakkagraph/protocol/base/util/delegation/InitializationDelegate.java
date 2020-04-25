package io.github.anticipasean.girakkagraph.protocol.base.util.delegation;

import akka.actor.ActorPath;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.japi.Pair;
import io.github.anticipasean.girakkagraph.protocol.base.actor.BaseActor;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.command.monitoring.IssueTimeout;
import io.github.anticipasean.girakkagraph.protocol.base.command.monitoring.IssueTimeoutImpl;
import io.github.anticipasean.girakkagraph.protocol.base.command.status.Initialized;
import io.github.anticipasean.girakkagraph.protocol.base.command.status.InitializedImpl;
import io.github.anticipasean.girakkagraph.protocol.base.command.status.StartUp;
import io.github.anticipasean.girakkagraph.protocol.base.command.status.StartUpImpl;
import io.github.anticipasean.girakkagraph.protocol.base.exception.ProtocolInitializationException;
import io.github.anticipasean.girakkagraph.protocol.base.role.Role;
import io.github.anticipasean.girakkagraph.protocol.base.role.Roles;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.Subordinate;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SubordinateSpawner;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class InitializationDelegate extends BaseActor<Command> {
  private static final int maximumNumberOfTimeoutsAllowed = 3;
  private final UUID startUpUUIDToBeUsedInInitializedReply;
  private final Optional<ActorRef<Initialized>> replyToAfterInitializationComplete;
  private final ActorRef<Command> parentActorRef;
  private final ActorPath parentActorPath;
  private final AtomicInteger numberOfTimeoutsReached = new AtomicInteger();
  private final Map<UUID, Boolean> uuidToInitializationReceived;
  private final StartUp startup;
  private final Map<UUID, Subordinate<Command>> initializedIdToSubordinateInfoMap;
  private final String id;

  protected InitializationDelegate(
      ActorContext<Command> context,
      String id,
      StartUp startUp,
      ActorRef<Command> parentActorRef,
      Map<UUID, Subordinate<Command>> initializedIdToSubordinateInfoMap) {
    super(context);
    this.startup = startUp;
    this.parentActorRef = parentActorRef;
    this.initializedIdToSubordinateInfoMap = initializedIdToSubordinateInfoMap;
    this.startUpUUIDToBeUsedInInitializedReply = startUp.commandId();
    this.replyToAfterInitializationComplete = startUp.replyTo();
    this.id = id;
    this.uuidToInitializationReceived =
        this.initializedIdToSubordinateInfoMap.keySet().stream()
            .map(uuid -> Pair.create(uuid, Boolean.FALSE))
            .collect(Collectors.toConcurrentMap(Pair::first, Pair::second));
    this.context.setReceiveTimeout(
        Duration.ofSeconds(30),
        IssueTimeoutImpl.builder().timeOutSetting(Duration.ofSeconds(30)).build());
    this.parentActorPath = parentActorRef.path();
  }

  public static BiFunction<ActorContext<Command>, StartUp, Behavior<Command>>
      startUpActionUsingInitializationDelegateToStartUpSubordinates(
          final List<Subordinate<Command>> subordinates) {
    return (context, startUp) -> {
      final String initializationDelegateId =
          String.format("%s_initialization", context.getSelf().path().name());
      final Map<UUID, Subordinate<Command>> initializedIdToSubordinateInfoMap =
          subordinates.stream()
              .map(entry -> Pair.create(UUID.randomUUID(), entry))
              .collect(
                  Collectors.toConcurrentMap(
                      uuidEntryPair -> uuidEntryPair.first(),
                      uuidEntryPair -> uuidEntryPair.second()));
      final Behavior<Command> initializationBehavior =
          Behaviors.setup(
              ctx -> {
                return new InitializationDelegate(
                    ctx,
                    initializationDelegateId,
                    startUp,
                    context.getSelf(),
                    initializedIdToSubordinateInfoMap);
              });
      ActorRef<Command> initializationActor =
          context.spawn(initializationBehavior, initializationDelegateId);
      initializedIdToSubordinateInfoMap.entrySet().stream()
          .forEach(
              entry -> {
                ActorRef<Command> subordinateRef = entry.getValue().subordinateRef();
                context
                    .getLog()
                    .info(
                        initializationDelegateId
                            + ": sending start up command to subordinate: "
                            + subordinateRef);
                subordinateRef.tell(
                    StartUpImpl.builder()
                        .replyTo(initializationActor.narrow())
                        .commandId(entry.getKey())
                        .build());
              });
      return Behaviors.same();
    };
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder()
        .onMessage(
            IssueTimeout.class,
            cmd -> numberOfTimeoutsReached.get() < maximumNumberOfTimeoutsAllowed,
            this::onIssueTimeoutWhereLimitNotReached)
        .onMessage(
            IssueTimeout.class,
            cmd -> numberOfTimeoutsReached.get() >= maximumNumberOfTimeoutsAllowed,
            this::onIssueTimeoutWhereLimitReached)
        .onMessage(
            Initialized.class,
            cmd -> cmd.errorOccurred().isPresent(),
            this::onInitializedWithErrorOccurred)
        .onMessage(
            Initialized.class,
            cmd -> !cmd.errorOccurred().isPresent(),
            this::onInitializedWithoutError)
        .build();
  }

  @Override
  public Role role() {
    return Roles.PROTOCOL_HANDLER;
  }

  private Behavior<Command> onIssueTimeoutWhereLimitNotReached(IssueTimeout cmd) {
    context.getLog().error(id + ": has reached a timeout");
    numberOfTimeoutsReached.addAndGet(1);
    return Behaviors.same();
  }

  private Behavior<Command> onIssueTimeoutWhereLimitReached(IssueTimeout cmd) {
    context
        .getLog()
        .error(
            id + ": number of timeouts has reached the limit of " + maximumNumberOfTimeoutsAllowed);
    context
        .getLog()
        .warn(
            id
                + ": there are "
                + uuidToInitializationReceived.values().stream().filter(b -> !b).count()
                + " subordinates that have yet to report being initialized");
    replyToAfterInitializationComplete.ifPresent(
        ref ->
            ref.tell(
                InitializedImpl.builder()
                    .actorPath(parentActorPath)
                    .errorOccurred(
                        ProtocolInitializationException.<Command>builder()
                            .actorRef(parentActorRef)
                            .cause(new TimeoutException())
                            .message(
                                "did not receive an initialized message for "
                                    + " within "
                                    + maximumNumberOfTimeoutsAllowed
                                    + " of "
                                    + cmd.timeOutSetting())
                            .build())
                    .build()));
    return Behaviors.stopped();
  }

  private Behavior<Command> onInitializedWithErrorOccurred(Initialized cmd) {
    context
        .getLog()
        .error(
            id
                + ": received an error for the initialization of [ "
                + cmd.actorPath().toSerializationFormat());
    replyToAfterInitializationComplete.ifPresent(
        ref ->
            ref.tell(
                InitializedImpl.builder()
                    .actorPath(parentActorPath)
                    .errorOccurred(cmd.errorOccurred())
                    .build()));
    return Behaviors.stopped();
  }

  private Behavior<Command> onInitializedWithoutError(Initialized cmd) {
    if (uuidToInitializationReceived.containsKey(cmd.commandId())
        && !uuidToInitializationReceived.get(cmd.commandId())) {
      uuidToInitializationReceived.put(cmd.commandId(), Boolean.TRUE);
      context
          .getLog()
          .info(
              id
                  + ": "
                  + cmd.actorPath().toSerializationFormat()
                  + " has reported being initialized");
    }
    if (uuidToInitializationReceived.values().stream().allMatch(Boolean::booleanValue)) {
      context
          .getLog()
          .info(id + ": all subordinates have returned initialized messages for " + parentActorRef);
      replyToAfterInitializationComplete.ifPresent(
          ref ->
              ref.tell(
                  InitializedImpl.builder()
                      .commandId(startUpUUIDToBeUsedInInitializedReply)
                      .actorPath(parentActorPath)
                      .build()));
      return Behaviors.stopped();
    }
    return Behaviors.same();
  }

  @Override
  protected SubordinateSpawner<Command> spawnSubordinatesOnContextCreation() {
    return newSubordinateSpawner();
  }
}
