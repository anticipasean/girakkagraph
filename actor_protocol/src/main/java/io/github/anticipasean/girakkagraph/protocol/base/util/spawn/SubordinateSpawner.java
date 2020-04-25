package io.github.anticipasean.girakkagraph.protocol.base.util.spawn;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.japi.Pair;
import akka.japi.function.Function4;
import io.github.anticipasean.girakkagraph.protocol.base.util.monitoring.SubordinateActorMonitorActivator;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.RegistrableImpl;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SubordinateSpawner<C> {
  private final Stream.Builder<Pair<ActorContext<C>, Spawnable<C, ?>>> streamBuilder;
  private final Consumer<Subordinate<C>> subordinateConsumer;
  private final SubordinateActorMonitorActivator<C> subordinateActorMonitorActivator;
  private final ActorContext<C> context;

  public SubordinateSpawner(ActorContext<C> context) {
    this(context, null, null);
  }

  public SubordinateSpawner(
      ActorContext<C> context,
      SubordinateActorMonitorActivator<C> subordinateActorMonitorActivator) {
    this(context, subordinateActorMonitorActivator, null);
  }

  public SubordinateSpawner(ActorContext<C> context, Consumer<Subordinate<C>> subordinateConsumer) {
    this(context, null, subordinateConsumer);
  }

  public SubordinateSpawner(
      ActorContext<C> context,
      SubordinateActorMonitorActivator<C> subordinateActorMonitorActivator,
      Consumer<Subordinate<C>> subordinateConsumer) {
    this.context = context;
    this.streamBuilder = Stream.builder();
    this.subordinateActorMonitorActivator = subordinateActorMonitorActivator;
    this.subordinateConsumer = subordinateConsumer;
  }

  //  public SubordinateSpawner<C> addSpawnableUsingParams(
  //      Class<? extends C> topLevelCommandHandled,
  //      String id,
  //      BiFunction<String, ActorRef<C>, Behavior<C>> createBehaviorFuncForSubordinateBehavior) {
  //    return addSpawnable(
  //        SpawnableImpl.builder()
  //            .idAndParentRefCreateBehaviorFunction(createBehaviorFuncForSubordinateBehavior)
  //            .id(id)
  //            .commandType(topLevelCommandHandled)
  //            .build());
  //  }

  public <T> SubordinateSpawner<C> addSpawningBehavior(
      Class<T> registeredCommand, SpawnFromParentRefBehavior<C> spawnFromParentRefBehavior) {
    Function<SpawnedContext<C, T>, Behavior<C>> spawnFromContextBehavior =
        spawnFromParentRefBehavior.compose(SpawnedContext::parentRef);
    addSpawningBehavior(registeredCommand, spawnFromContextBehavior::apply);
    return this;
  }

  public <T> SubordinateSpawner<C> addSpawningBehavior(
      Class<T> registeredCommand, SpawnFromContextBehavior<C, T> spawnFromContextBehavior) {
    Spawnable<C, T> spawnable =
        SpawnableImpl.<C, T>builder()
            .registrable(RegistrableImpl.of(registeredCommand))
            .spawnFromContextBehavior(spawnFromContextBehavior)
            .build();
    addSpawnable(spawnable);
    return this;
  }

  public SubordinateSpawner<C> addSpawnable(Spawnable<C, ?> spawnable) {
    try {
      final Pair<ActorContext<C>, Spawnable<C, ?>> pair = Pair.create(context, spawnable);
      streamBuilder.add(pair);
    } catch (Exception e) {
      context
          .getLog()
          .error(
              "failure to initialize all dependent actors for {} ; beginning termination",
              context.getSelf().path().toSerializationFormat());
      context.getSystem().terminate();
    }
    return this;
  }

  public List<Subordinate<C>> spawnSubordinates() {
    List<Subordinate<C>> subordinates =
        streamBuilder
            .build()
            .map(useContextAndSpawnableToCreateSubordinate())
            .collect(Collectors.toList());
    if (subordinateConsumer != null) {
      applyGivenConsumerToSubordinateList(subordinates);
    }
    if (subordinateActorMonitorActivator != null) {
      activateSubordinateActorMonitoring(subordinates);
    }
    return subordinates;
  }

  private Function<Pair<ActorContext<C>, Spawnable<C, ?>>, Subordinate<C>>
      useContextAndSpawnableToCreateSubordinate() {
    return (pair) -> {
      ActorContext<C> context = pair.first();
      Spawnable<C, ?> spawnable = pair.second();
      ActorRef<C> spawnRef =
          context.spawn(BehaviorCreator.create(context.getSelf(), spawnable), spawnable.id());
      return (Subordinate<C>)
          SubordinateImpl.<C>builder().spawnable(spawnable).subordinateRef(spawnRef).build();
    };
  }

  private void generateSubordinateTerminatedMessageOnStop(
      ActorContext<C> context,
      ActorRef<C> subordinateRef,
      Class<?> registeredCommandType,
      String subordinateId,
      Function4<ActorContext<C>, ActorRef<C>, Class<?>, String, C>
          subordinateTerminatedMessageGenerator) {
    try {
      context.watchWith(
          subordinateRef,
          subordinateTerminatedMessageGenerator.apply(
              context, subordinateRef, registeredCommandType, subordinateId));
    } catch (Exception e) {
      context
          .getLog()
          .error(
              String.format(
                  "unable to generate subordinate terminated message for subordinate ref: [ %s ]",
                  subordinateRef.path()),
              e);
    }
  }

  private void applyGivenConsumerToSubordinateList(List<Subordinate<C>> subordinates) {
    subordinates.stream().forEach(subordinateConsumer);
  }

  private void activateSubordinateActorMonitoring(List<Subordinate<C>> subordinates) {
    boolean successfulActivation =
        subordinates.stream()
            .allMatch(
                subordinate -> {
                  try {
                    return Optional.of(
                            subordinateActorMonitorActivator.apply(
                                context,
                                subordinate.subordinateRef(),
                                subordinate.spawnable().protocolMessageType(),
                                subordinate.spawnable().id()))
                        .orElse(Boolean.FALSE);
                  } catch (Exception e) {
                    return Boolean.FALSE;
                  }
                });
    if (!successfulActivation) {
      context
          .getLog()
          .error(
              String.format(
                  "not all subordinate actors are equipped with monitoring for %s",
                  context.getSelf()));
    }
  }
}
