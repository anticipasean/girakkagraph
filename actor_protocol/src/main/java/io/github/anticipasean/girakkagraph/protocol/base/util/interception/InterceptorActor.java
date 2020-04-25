package io.github.anticipasean.girakkagraph.protocol.base.util.interception;

import akka.actor.typed.Behavior;
import akka.actor.typed.BehaviorInterceptor;
import akka.actor.typed.Signal;
import akka.actor.typed.TypedActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.StashBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import scala.reflect.ClassTag;

public class InterceptorActor<Ctx, Res extends Ctx, TimeO extends Ctx>
    extends BehaviorInterceptor<Ctx, Ctx> {

  private final InterceptorPlan<Ctx, Res, TimeO> interceptorPlan;
  private final StashBuffer<Ctx> stashBuffer;
  private final AtomicInteger numberOfTimeoutsOccurred;

  protected InterceptorActor(
      Class<Ctx> interceptMessageClass,
      InterceptorPlan<Ctx, Res, TimeO> interceptorPlan,
      StashBuffer<Ctx> stashBuffer) {
    super(interceptMessageClass);
    this.interceptorPlan = interceptorPlan;
    this.stashBuffer = stashBuffer;
    numberOfTimeoutsOccurred = new AtomicInteger(0);
  }

  protected InterceptorActor(
      ClassTag<Ctx> interceptMessageClassTag,
      InterceptorPlan<Ctx, Res, TimeO> interceptorPlan,
      StashBuffer<Ctx> stashBuffer) {
    super(interceptMessageClassTag);
    this.interceptorPlan = interceptorPlan;
    this.stashBuffer = stashBuffer;
    numberOfTimeoutsOccurred = new AtomicInteger(0);
  }

  protected InterceptorActor(
      InterceptorPlan<Ctx, Res, TimeO> interceptorPlan, StashBuffer<Ctx> stashBuffer) {
    super(interceptorPlan.holdAllOthersOfType());
    this.interceptorPlan = interceptorPlan;
    this.stashBuffer = stashBuffer;
    this.numberOfTimeoutsOccurred = new AtomicInteger(0);
    interceptorPlan.startingWithMessage().ifPresent(this.stashBuffer::stash);
  }

  public static <Ctx, Res extends Ctx, TimeO extends Ctx> Behavior<Ctx> create(
      InterceptorPlan<Ctx, Res, TimeO> interceptorPlan, Behavior<Ctx> behaviorBeingIntercepted) {
    return Behaviors.withStash(
        interceptorPlan.bufferingUpToNMessages(),
        stashBuffer ->
            Behaviors.intercept(
                () -> new InterceptorActor<Ctx, Res, TimeO>(interceptorPlan, stashBuffer),
                behaviorBeingIntercepted));
  }

  @Override
  public Class<Ctx> interceptMessageClass() {
    return super.interceptMessageClass();
  }

  @Override
  public Behavior<Ctx> aroundStart(TypedActorContext<Ctx> ctx, PreStartTarget<Ctx> target) {
    ctx.asJava()
        .getLog()
        .info(
            "interceptor_actor: starting interception of "
                + interceptorPlan.holdAllOthersOfType().getSimpleName());
    return super.aroundStart(ctx, target);
  }

  @Override
  public Behavior<Ctx> aroundSignal(
      TypedActorContext<Ctx> ctx, Signal signal, SignalTarget<Ctx> target) {
    return super.aroundSignal(ctx, signal, target);
  }

  @Override
  public boolean isSame(BehaviorInterceptor<Object, Object> other) {
    return super.isSame(other);
  }

  @Override
  public Behavior<Ctx> aroundReceive(
      TypedActorContext<Ctx> ctx, Ctx msg, ReceiveTarget<Ctx> target) {
    ctx.asJava().getLog().info("interceptor_actor: received " + msg);
    return onMessageReceived(ctx, msg, target);
  }

  private Behavior<Ctx> onMessageReceived(
      TypedActorContext<Ctx> ctx, Ctx msg, ReceiveTarget<Ctx> target) {
    if (isInstanceOfExpectedResponseType(msg)) {
      ctx.asJava()
          .getLog()
          .info("interceptor_actor: received msg matching expected type and predicate");
      return handleExpectedResponseTypeReceived(ctx, msg, target);
    } else if (isInstanceOfTimeoutType(msg)) {
      ctx.asJava().getLog().info("interceptor_actor: received timeout message");
      TimeO timeoutTypeCommand = interceptorPlan.ofType().cast(msg);
      if (isWithinTimeoutLimit()) {
        return renewTimeout(ctx);
      } else {
        ctx.asJava()
            .getLog()
            .info("interceptor_actor: received timeout msg and reached max number of timeouts");
        return handleMaximumNumberOfTimeoutsReached(ctx, target);
      }
    } else if (isInstanceOfTypeToStash(msg)) {
      if (!stashBuffer.isFull()) {
        ctx.asJava()
            .getLog()
            .info(
                "interceptor_actor: stashing another msg of type: "
                    + msg.getClass().getSimpleName());
        stashBuffer.stash(msg);
        return Behaviors.same();
      } else {
        ctx.asJava().getLog().info("interceptor_actor: received another msg but buffer is full");
        return handleBufferFull(ctx, target);
      }
    } else {
      ctx.asJava()
          .getLog()
          .info("interceptor_actor: passing on msg of type: " + msg.getClass().getSimpleName());
      target.apply(ctx, msg);
    }
    return Behaviors.same();
  }

  private boolean isInstanceOfTypeToStash(Ctx msg) {
    return interceptorPlan.holdAllOthersOfType().isAssignableFrom(msg.getClass())
        && interceptorPlan.matching().test(msg);
  }

  private boolean isInstanceOfExpectedResponseType(Ctx msg) {
    return interceptorPlan.waitForResponseOfType().isAssignableFrom(msg.getClass())
        && interceptorPlan
            .whereResponseMatches()
            .test(interceptorPlan.waitForResponseOfType().cast(msg));
  }

  private Behavior<Ctx> handleExpectedResponseTypeReceived(
      TypedActorContext<Ctx> ctx, Ctx msg, ReceiveTarget<Ctx> target) {
    Ctx replyToActorBeingIntercepted =
        interceptorPlan
            .beforeResponseAndOthersReleased()
            .apply(interceptorPlan.waitForResponseOfType().cast(msg), null);
    AtomicReference<Behavior<Ctx>> behaviorHolder = new AtomicReference<>();
    behaviorHolder.set(target.apply(ctx, replyToActorBeingIntercepted));
    return freeBufferedMessagesAndReturnToActorBehaviorBeingIntercepted(
        behaviorHolder, ctx, target);
  }

  private Behavior<Ctx> freeBufferedMessagesAndReturnToActorBehaviorBeingIntercepted(
      AtomicReference<Behavior<Ctx>> behaviorHolder,
      TypedActorContext<Ctx> ctx,
      ReceiveTarget<Ctx> target) {
    ctx.asJava().getLog().info("interceptor_actor: releasing buffered messages");
    Set<Ctx> messageSetToAvoidDuplication = new HashSet<>();
    stashBuffer.forEach(
        msg -> {
          if (!messageSetToAvoidDuplication.contains(msg)) {
            messageSetToAvoidDuplication.add(msg);
            behaviorHolder.set(target.apply(ctx, msg));
          }
        });
    return behaviorHolder.get();
  }

  private boolean isInstanceOfTimeoutType(Ctx msg) {
    return interceptorPlan.ofType().isAssignableFrom(msg.getClass());
  }

  private Behavior<Ctx> renewTimeout(TypedActorContext<Ctx> ctx) {
    ctx.asJava()
        .getLog()
        .info(
            "interceptor_actor: renewing timeout for "
                + interceptorPlan.waitForResponseOfType().getSimpleName());
    ctx.asJava()
        .setReceiveTimeout(
            interceptorPlan.within(),
            interceptorPlan.withTimeout().apply(interceptorPlan.within()));
    numberOfTimeoutsOccurred.addAndGet(1);
    return Behaviors.same();
  }

  private Behavior<Ctx> handleMaximumNumberOfTimeoutsReached(
      TypedActorContext<Ctx> ctx, ReceiveTarget<Ctx> target) {
    TimeoutException timeoutException =
        new TimeoutException(
            "did not receive a response of type "
                + interceptorPlan.waitForResponseOfType().getName()
                + " within limit of "
                + interceptorPlan.within().multipliedBy(interceptorPlan.nTimes()));
    Ctx replyToBehaviorIntercepted =
        interceptorPlan.beforeResponseAndOthersReleased().apply(null, timeoutException);
    return stashBuffer.unstashAll(target.apply(ctx, replyToBehaviorIntercepted));
  }

  private Behavior<Ctx> handleBufferFull(TypedActorContext<Ctx> ctx, ReceiveTarget<Ctx> target) {
    IllegalStateException illegalStateException =
        new IllegalStateException("buffer for interceptor is full. unable to store more messages");
    Ctx replyToActorBeingIntercepted =
        interceptorPlan.beforeResponseAndOthersReleased().apply(null, illegalStateException);
    return target.apply(ctx, replyToActorBeingIntercepted);
  }

  private boolean isWithinTimeoutLimit() {
    return numberOfTimeoutsOccurred.get() < interceptorPlan.nTimes();
  }
}
