package io.github.anticipasean.girakkagraph.protocol.base.util.delegation;

import akka.actor.typed.ActorRef;
import java.time.Duration;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.immutables.value.Value;

@Value.Style(
    stagedBuilder = true,
    typeImmutable = "*Impl",
    typeAbstract = "*",
    overshadowImplementation = true)
@Value.Immutable
public interface StallingDelegatePlan<Req, Cond> extends DelegatePlan<Req> {

  @Value.Derived
  @Override
  default String delegateName() {
    return String.join(
        "_",
        onBehalfOf().path().name(),
        stallRespondingToType().getSimpleName(),
        "stalling",
        "delegate");
  }

  ActorRef<Req> onBehalfOf();

  Class<Req> stallRespondingToType();

  Predicate<Req> matching();

  Supplier<Cond> untilSupplier();

  Predicate<Supplier<Cond>> meetsCondition();

  Integer checkingNTimes();

  Duration within();

  BiConsumer<List<Req>, Throwable> orElse();
}
