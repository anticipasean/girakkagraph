package io.github.anticipasean.girakkagraph.protocol.base.actor;

import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.Subordinate;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SubordinateSpawner;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public abstract class BaseActor<C> extends AbstractBehavior<C> implements Actor {

  protected final ActorContext<C> context;
  private final List<Subordinate<C>> subordinates;

  protected BaseActor(ActorContext<C> context) {
    super(context);
    this.context = context;
    this.subordinates = spawnSubordinatesOnContextCreation().spawnSubordinates();
  }

  protected ActorContext<C> context() {
    return context;
  }

  protected abstract SubordinateSpawner<C> spawnSubordinatesOnContextCreation();

  protected List<Subordinate<C>> subordinates() {
    return subordinates;
  }

  protected Optional<Subordinate<C>> firstSubordinateMatching(Predicate<Subordinate<C>> predicate) {
    return subordinates.stream().filter(predicate).findFirst();
  }

  protected Optional<Subordinate<C>> subordinateById(String id) {
    return subordinates.stream().filter(subordinate -> subordinate.id().equals(id)).findFirst();
  }

  protected Optional<Subordinate<C>> subordinateByCommandTypeHandled(
      Class<? extends C> commandType) {
    return subordinates.stream()
        .filter(subordinate -> subordinate.commandType().equals(commandType))
        .findFirst();
  }

  protected SubordinateSpawner<C> newSubordinateSpawner() {
    return new SubordinateSpawner<>(context);
  }
}
