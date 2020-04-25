package io.github.anticipasean.girakkagraph.protocol.base.util.interception;

import java.time.Duration;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import org.immutables.value.Value;

@Value.Style(
    stagedBuilder = true,
    typeImmutable = "*Impl",
    typeAbstract = "*",
    overshadowImplementation = true)
@Value.Immutable
public interface InterceptorPlan<Ctx, Res extends Ctx, TimeO extends Ctx> {

  Class<Res> waitForResponseOfType();

  Predicate<Res> whereResponseMatches();

  Function<Duration, TimeO> withTimeout();

  Class<TimeO> ofType();

  Integer nTimes();

  Duration within();

  Class<Ctx> holdAllOthersOfType();

  Predicate<Ctx> matching();

  Integer bufferingUpToNMessages();

  BiFunction<Res, Throwable, Ctx> beforeResponseAndOthersReleased();

  Optional<Ctx> startingWithMessage();
}
