package io.github.anticipasean.girakkagraph.protocol.base.util.spawn;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.Behaviors;
import io.github.anticipasean.girakkagraph.protocol.base.util.monitoring.SupervisionPolicy;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.Registrable;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.RegistrableImpl;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public interface BehaviorCreator<C> {

  static <C, T, S extends Spawnable<C, T>> Behavior<C> create(
      ActorRef<C> parentActorRef, S spawnable) {
    return Behaviors.setup(
        context -> {
          return spawnable
              .spawnFromContextBehavior()
              .apply(SpawnedContext.newInstance(context, parentActorRef, spawnable));
        });
  }

  static <C, T, S extends SpawnFromContextBehavior<C, T>> Behavior<C> create(
      ActorRef<C> parentActorRef, Registrable<T> registrable, S spawnFromContextBehavior) {
    return create(
        parentActorRef,
        SpawnableImpl.<C, T>builder()
            .registrable(registrable)
            .spawnFromContextBehavior(spawnFromContextBehavior)
            .build());
  }

  static <C, T, S extends SpawnFromContextBehavior<C, T>> Behavior<C> create(
      ActorRef<C> parentActorRef, Class<T> registrableCommandType, S spawnFromContextBehavior) {
    return create(
        parentActorRef, RegistrableImpl.of(registrableCommandType), spawnFromContextBehavior);
  }

  static <C, S extends SpawnFromParentRefBehavior<C>> Behavior<C> create(
      ActorRef<C> parentActorRef, S spawnFromParentRefBehavior) {
    return spawnFromParentRefBehavior.apply(parentActorRef);
  }

  /**
   * Supervision policies can only be added to instantiated behaviors/actors---
   * those who have had their constructors called with a context provided
   * so SpawnFromParentRefBehavior functions must be used
   * instead of SpawnFromContextBehavior functions.
   * @param spawnFromParentRefBehavior
   * @param failureType
   * @param supervisorStrategy
   * @param <C> Context Message Protocol e.g. Command
   * @param <S> Spawn From Parent Ref Behavior Type Function {@code
   *     Function<ActorRef<C>,Behavior<C>> }
   * @return
   */
  static <C, S extends SpawnFromParentRefBehavior<C>>
      SpawnFromParentRefBehavior<C> spawnFromParentRefBehaviorWithSupervision(
          S spawnFromParentRefBehavior,
          Class<? extends Throwable> failureType,
          SupervisorStrategy supervisorStrategy) {
    return parentRef ->
        spawnFromParentRefBehavior
            .andThen(
                behavior ->
                    Behaviors.supervise(behavior).onFailure(failureType, supervisorStrategy))
            .apply(parentRef);
  }

  static <C, S extends SpawnFromParentRefBehavior<C>>
      SpawnFromParentRefBehavior<C> applySupervisionPoliciesToSpawnFromContextBehavior(
          S spawnFromParentRefBehavior, List<SupervisionPolicy> supervisionPolicyList) {
    AtomicReference<SpawnFromParentRefBehavior<C>> behaviorHolder =
        new AtomicReference<>(spawnFromParentRefBehavior);
    supervisionPolicyList.stream()
        .sorted(Comparator.comparingInt(SupervisionPolicy::priority))
        .forEach(
            supervisionPolicy -> {
              behaviorHolder.updateAndGet(
                  spawnFromCtxBehav ->
                      spawnFromParentRefBehaviorWithSupervision(
                          spawnFromCtxBehav,
                          supervisionPolicy.failureType(),
                          supervisionPolicy.supervisorStrategy()));
            });
    return behaviorHolder.get();
  }
}
