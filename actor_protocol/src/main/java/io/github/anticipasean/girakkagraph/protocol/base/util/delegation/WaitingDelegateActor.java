package io.github.anticipasean.girakkagraph.protocol.base.util.delegation;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.github.anticipasean.girakkagraph.protocol.base.actor.BaseActor;
import io.github.anticipasean.girakkagraph.protocol.base.handler.ProtocolHandler;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SubordinateSpawner;
import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

public class WaitingDelegateActor<C, Req, Res> extends BaseActor<WaitingDelegateActor.Response>
    implements ProtocolHandler {

  private final WaitingDelegatePlan<C, Req, Res> waitingDelegatePlan;

  private final Duration timeoutOnResponse;
  private final ActorRef<Res> delegateRefForResRepliesWrapping;
  private final Duration timeoutBetweenRetryAttempts;
  private final Req request;
  private final ActorRef<Req> requestRecipient;
  private int numberOfAttemptsMade;

  protected WaitingDelegateActor(
      ActorContext<Response> context, WaitingDelegatePlan<C, Req, Res> waitingDelegatePlan) {
    super(context);
    this.waitingDelegatePlan = waitingDelegatePlan;
    context.setLoggerName(
        String.join(
            "_", waitingDelegatePlan.onBehalfOf().path().elements().last(), "waiting_delegate"));
    this.timeoutOnResponse = waitingDelegatePlan.within();
    this.numberOfAttemptsMade = waitingDelegatePlan.nTimes();
    if (numberOfAttemptsMade <= 0) {
      throw new IllegalArgumentException("nTimes for retry attempts may not be <= 0");
    }
    this.timeoutBetweenRetryAttempts = timeoutOnResponse.dividedBy(numberOfAttemptsMade);
    this.delegateRefForResRepliesWrapping =
        context.messageAdapter(waitingDelegatePlan.forResponseOfType(), Response::new);
    this.requestRecipient = this.waitingDelegatePlan.to();
    this.request = this.waitingDelegatePlan.makeRequest().apply(delegateRefForResRepliesWrapping);
    this.requestRecipient.tell(request);
    this.context.setReceiveTimeout(this.timeoutBetweenRetryAttempts, new Timeout<>());
  }

  public static <C, Req, Res> Behavior<Response> createDelegateBehaviorFollowingPlan(
      WaitingDelegatePlan<C, Req, Res> waitingDelegatePlan) {
    return Behaviors.setup(
        context -> {
          return new WaitingDelegateActor<C, Req, Res>(context, waitingDelegatePlan);
        });
  }

  @Override
  public Receive<Response> createReceive() {
    return newReceiveBuilder()
        .onMessage(Timeout.class, this::onTimeoutReceived)
        .onMessage(Response.class, this::onWrappedReply)
        .build();
  }

  private Behavior<Response> onTimeoutReceived(Timeout timeout) {
    context.getLog().info("timeout received: " + timeout);
    if (numberOfAttemptsMade > 0) {
      numberOfAttemptsMade--;
      //      this.requestRecipient.tell(request);
      this.context.cancelReceiveTimeout();
      this.context.setReceiveTimeout(this.timeoutBetweenRetryAttempts, new Timeout<Res>());
      return Behaviors.same();
    }
    context
        .getLog()
        .warn(
            String.format(
                "timeout on reply action when response received by delegate: "
                    + "[ expectedResponseType: %s, callingActorContextRef: %s, timedOutAfter: %s ] ",
                waitingDelegatePlan.forResponseOfType().getName(),
                waitingDelegatePlan.onBehalfOf(),
                timeoutOnResponse));
    String errorMessage =
        new StringBuilder("delegate did not receive reply after ")
            .append(this.waitingDelegatePlan.nTimes())
            .append(" attempts within ")
            .append(this.waitingDelegatePlan.within())
            .toString();
    C replyToCaller =
        waitingDelegatePlan.whenCompleteSendBack().apply(null, new TimeoutException(errorMessage));
    waitingDelegatePlan.onBehalfOf().tell(replyToCaller);
    return Behaviors.stopped();
  }

  private Behavior<Response> onWrappedReply(Response response) {
    context.getLog().info("wrapped_reply received: " + response);
    if (response.getValueMaybe().isPresent()) {
      return onReplyReceivedWithValuePresent(response);
    } else {
      return onReplyReceivedButValueNotPresent(response);
    }
  }

  private Behavior<Response> onReplyReceivedWithValuePresent(Response response) {
    if (response.getValueMaybe().isPresent()
        && waitingDelegatePlan
            .forResponseOfType()
            .isAssignableFrom(response.getValueMaybe().get().getClass())) {
      handleReplyWithExpectedValueReceived(
          waitingDelegatePlan.forResponseOfType().cast(response.getValueMaybe().get()));
    } else {
      String errorMessage =
          new StringBuilder("delegate did not receive reply of expected type: ")
              .append(waitingDelegatePlan.forResponseOfType().getName())
              .toString();
      C replyToCaller =
          waitingDelegatePlan
              .whenCompleteSendBack()
              .apply(null, new NoSuchElementException(errorMessage));
      waitingDelegatePlan.onBehalfOf().tell(replyToCaller);
    }
    return Behaviors.stopped();
  }

  private void handleReplyWithExpectedValueReceived(Res expectedValue) {
    waitingDelegatePlan
        .onBehalfOf()
        .tell(waitingDelegatePlan.whenCompleteSendBack().apply(expectedValue, null));
  }

  private Behavior<Response> onReplyReceivedButValueNotPresent(Response response) {
    IllegalArgumentException illegalArgumentException =
        new IllegalArgumentException(
            "null was returned as a value for delegate request to " + waitingDelegatePlan.to());
    context
        .getLog()
        .error(
            String.format(
                "null value returned for delegate request: [ expectedType: %s, actualValue: %s ] ",
                waitingDelegatePlan.forResponseOfType().getName(), null),
            illegalArgumentException);
    C replyToCaller =
        waitingDelegatePlan.whenCompleteSendBack().apply(null, illegalArgumentException);
    waitingDelegatePlan.onBehalfOf().tell(replyToCaller);
    return Behaviors.stopped();
  }

  @Override
  protected SubordinateSpawner<Response> spawnSubordinatesOnContextCreation() {
    return newSubordinateSpawner();
  }

  public static class Response<Res> {
    private Optional<Res> valueMaybe;

    Response(Res value) {
      valueMaybe = Optional.ofNullable(value);
    }

    @Override
    public String toString() {
      return new StringBuilder(this.getClass().getSimpleName())
          .append("[")
          .append(getValueMaybe())
          .append("]")
          .toString();
    }

    public Optional<Res> getValueMaybe() {
      return valueMaybe;
    }
  }

  private static class Timeout<Res> extends Response<Res> {
    Timeout() {
      super(null);
    }

    @Override
    public String toString() {
      return new StringBuilder(this.getClass().getSimpleName())
          .append("[")
          .append(getValueMaybe())
          .append("]")
          .toString();
    }
  }
}
