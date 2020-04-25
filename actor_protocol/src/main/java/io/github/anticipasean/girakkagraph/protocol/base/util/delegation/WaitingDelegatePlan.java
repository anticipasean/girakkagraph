package io.github.anticipasean.girakkagraph.protocol.base.util.delegation;

import akka.actor.typed.ActorRef;
import java.time.Duration;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.immutables.value.Value;

@Value.Style(
    stagedBuilder = true,
    typeImmutable = "*Impl",
    typeAbstract = "*",
    overshadowImplementation = true)
@Value.Immutable
public interface WaitingDelegatePlan<Ctx, Req, Res> extends DelegatePlan<Ctx> {

  @Override
  ActorRef<Ctx> onBehalfOf();

  Function<ActorRef<Res>, Req> makeRequest();

  ActorRef<Req> to();

  Class<Res> forResponseOfType();

  Integer nTimes();

  Duration within();

  BiFunction<Res, Throwable, Ctx> whenCompleteSendBack();

  @Value.Derived
  @Override
  default String delegateName() {
    return String.join(
        "_", onBehalfOf().path().name(), forResponseOfType().getSimpleName(), "delegate");
  }
}
