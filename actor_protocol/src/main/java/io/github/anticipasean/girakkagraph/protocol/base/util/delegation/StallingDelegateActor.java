package io.github.anticipasean.girakkagraph.protocol.base.util.delegation;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.StashBuffer;
import io.github.anticipasean.girakkagraph.protocol.base.actor.BaseActor;
import io.github.anticipasean.girakkagraph.protocol.base.handler.ProtocolHandler;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SubordinateSpawner;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class StallingDelegateActor<Req, Cond> extends BaseActor<StallingDelegateActor.Request>
    implements ProtocolHandler {

  private final StallingDelegatePlan<Req, Cond> stallingDelegatePlan;
  private final Duration timeoutOnResponse;
  private final ActorRef<Req> requestWrappingDelegateRef;
  private final Duration timeoutBetweenRetryAttempts;
  private final StashBuffer<Request> stashBuffer;
  private int numberOfAttemptsMade;

  protected StallingDelegateActor(
      ActorContext<Request> context,
      StallingDelegatePlan<Req, Cond> stallingDelegatePlan,
      StashBuffer<Request> stashBuffer) {
    super(context);
    this.stallingDelegatePlan = stallingDelegatePlan;
    this.stashBuffer = stashBuffer;
    context.setLoggerName(
        String.join(
            "_", stallingDelegatePlan.onBehalfOf().path().elements().last(), "stalling_delegate"));
    this.timeoutOnResponse = stallingDelegatePlan.within();
    this.numberOfAttemptsMade = stallingDelegatePlan.checkingNTimes();
    if (numberOfAttemptsMade <= 0) {
      throw new IllegalArgumentException("nTimes for retry attempts may not be <= 0");
    }
    this.timeoutBetweenRetryAttempts = timeoutOnResponse.dividedBy(numberOfAttemptsMade);
    this.requestWrappingDelegateRef =
        context.messageAdapter(stallingDelegatePlan.stallRespondingToType(), Request::new);
    this.context.setReceiveTimeout(this.timeoutBetweenRetryAttempts, new Timeout<>());
  }

  public static <Req, Cond> Behavior<Request> createDelegateBehaviorFollowingPlan(
      StallingDelegatePlan<Req, Cond> stallingDelegatePlan) {
    return Behaviors.setup(
        context -> {
          return Behaviors.withStash(
              1000,
              stashBuffer ->
                  new StallingDelegateActor<Req, Cond>(context, stallingDelegatePlan, stashBuffer));
        });
  }

  @Override
  public Receive<Request> createReceive() {
    return newReceiveBuilder()
        .onMessage(Timeout.class, this::onTimeoutReceived)
        .onMessage(Request.class, this::onWrappedRequest)
        .build();
  }

  private Behavior<Request> onTimeoutReceived(Timeout timeout) {
    context.getLog().info("timeout received: " + timeout);
    if (stallingDelegatePlan.meetsCondition().test(stallingDelegatePlan.untilSupplier())) {
      return handleSupplierConditionMet();
    }
    if (numberOfAttemptsMade > 0) {
      return resetNextCheckTimeoutForSupplierConditionBeingMet();
    }
    return stopStallingAndTimeout();
  }

  private Behavior<Request> handleSupplierConditionMet() {
    context
        .getLog()
        .info(
            String.format(
                "stalling condition supplier [ %s ] has met predicate [ %s ]\n"
                    + "will no longer stall requests [ type: %s ] matching criteria given",
                stallingDelegatePlan.untilSupplier().get(),
                stallingDelegatePlan.meetsCondition(),
                stallingDelegatePlan.stallRespondingToType().getSimpleName()));
    context
        .getLog()
        .info(
            String.format(
                "releasing stashed requests [ %d of type %s ] before stopping",
                stashBuffer.size(), stallingDelegatePlan.stallRespondingToType().getSimpleName()));
    stashBuffer.forEach(
        request ->
            stallingDelegatePlan
                .onBehalfOf()
                .tell(
                    stallingDelegatePlan
                        .stallRespondingToType()
                        .cast(request.getValueMaybe().get())));
    return Behaviors.stopped();
  }

  private Behavior<Request> resetNextCheckTimeoutForSupplierConditionBeingMet() {
    numberOfAttemptsMade--;
    this.context.cancelReceiveTimeout();
    this.context.setReceiveTimeout(this.timeoutBetweenRetryAttempts, new Timeout<Req>());
    return Behaviors.same();
  }

  private Behavior<Request> stopStallingAndTimeout() {
    context
        .getLog()
        .warn(
            String.format(
                "timeout exceeded on stalling delegate for conditional supplier supplying an object meeting "
                    + "the continuation conditional predicate"
                    + "[ stalling_request_type: %s, stalling_on_behalf_of: %s, timeout_duration: %s, condition_object_supplied: %s ] ",
                stallingDelegatePlan.stallRespondingToType().getName(),
                stallingDelegatePlan.onBehalfOf().path().toSerializationFormat(),
                timeoutOnResponse,
                stallingDelegatePlan.untilSupplier().get()));
    List<Req> unwrappedStalledRequests = getStalledRequests();
    stallingDelegatePlan.orElse().accept(unwrappedStalledRequests, null);
    return Behaviors.stopped();
  }

  private List<Req> getStalledRequests() {
    List<Request> requests = new ArrayList<>();
    stashBuffer.forEach(requests::add);
    return requests.stream()
        .map(Request::getValueMaybe)
        .map(Optional::get)
        .map(stallingDelegatePlan.stallRespondingToType()::cast)
        .collect(Collectors.toList());
  }

  private Behavior<Request> onWrappedRequest(Request request) {
    context.getLog().info("wrapped_reply received: " + request);
    if (request.getValueMaybe().isPresent()) {
      return onWrappedRequestReceivedWithValuePresent(request);
    } else {
      return onWrappedRequestReceivedButValueNotPresent(request);
    }
  }

  private Behavior<Request> onWrappedRequestReceivedWithValuePresent(Request request) {
    if (request.getValueMaybe().isPresent()
        && stallingDelegatePlan
            .stallRespondingToType()
            .isAssignableFrom(request.getValueMaybe().get().getClass())
        && stallingDelegatePlan
            .matching()
            .test(
                stallingDelegatePlan.stallRespondingToType().cast(request.getValueMaybe().get()))) {
      handleResponseRequiringStallingReceived(request);
    } else if (request.getValueMaybe().isPresent()
        && stallingDelegatePlan
            .stallRespondingToType()
            .isAssignableFrom(request.getValueMaybe().get().getClass())) {
      handleRequestNotRequiringStallingReceived(request);
    }
    return Behaviors.same();
  }

  private void handleRequestNotRequiringStallingReceived(Request request) {
    Object o = request.getValueMaybe().get();
    if (stallingDelegatePlan.stallRespondingToType().isAssignableFrom(o.getClass())) {
      Req req = stallingDelegatePlan.stallRespondingToType().cast(o);
      stallingDelegatePlan.onBehalfOf().tell(req);
    }
  }

  private void handleResponseRequiringStallingReceived(Request request) {
    stashBuffer.stash(request);
  }

  private Behavior<Request> onWrappedRequestReceivedButValueNotPresent(Request request) {
    IllegalArgumentException illegalArgumentException =
        new IllegalArgumentException(
            "null was returned as a value for delegate request to "
                + stallingDelegatePlan.onBehalfOf());
    context
        .getLog()
        .error(
            String.format(
                "null value returned for delegate request: [ expectedType: %s, actualValue: %s ] ",
                stallingDelegatePlan.stallRespondingToType().getName(), null),
            illegalArgumentException);
    stallingDelegatePlan.orElse().accept(getStalledRequests(), illegalArgumentException);
    return Behaviors.stopped();
  }

  @Override
  protected SubordinateSpawner<Request> spawnSubordinatesOnContextCreation() {
    return newSubordinateSpawner();
  }

  public static class Request<Res> {
    private Optional<Res> valueMaybe;

    Request(Res value) {
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

  private static class Timeout<Res> extends Request<Res> {

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
