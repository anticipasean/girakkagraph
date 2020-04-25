package io.github.anticipasean.girakkagraph.protocol.base.util.delegation;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.internal.adapter.ActorRefAdapter;
import akka.actor.typed.javadsl.ActorContext;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public interface DelegateConfigurable<C> {

  default <Ctx, Req, Res>
      ActorRef<WaitingDelegateActor.Response>
          letDelegateRequestAndWaitForResponseFollowingPlanInContext(
              WaitingDelegatePlan<Ctx, Req, Res> waitingDelegatePlan,
              ActorContext<Ctx> context,
              BiFunction<
                      ActorRef<WaitingDelegateActor.Response>,
                      WaitingDelegatePlan<Ctx, Req, Res>,
                      Ctx>
                  generateMessageForCallerWhenDelegateTerminated) {
    Optional<ActorRef<Void>> activeDelegateMaybe =
        context.getChild(waitingDelegatePlan.delegateName());
    if (activeDelegateMaybe.isPresent()) {
      // Hack done to return an appropriately typed delegate ref e.g.
      // ActorRef<DelegateBehavior.Response>
      // instead of an ActorRef<Void>
      // This hack assumes the name of the delegate is unique to the request being delegated at
      // the time
      return ActorRefAdapter.apply(ActorRefAdapter.toClassic(activeDelegateMaybe.get()));
    }
    Behavior<WaitingDelegateActor.Response> delegateBehaviorFollowingPlan =
        WaitingDelegateActor.createDelegateBehaviorFollowingPlan(waitingDelegatePlan);
    ActorRef<WaitingDelegateActor.Response> delegateRef =
        context.spawn(delegateBehaviorFollowingPlan, waitingDelegatePlan.delegateName());
    context.watchWith(
        delegateRef,
        generateMessageForCallerWhenDelegateTerminated.apply(delegateRef, waitingDelegatePlan));
    return delegateRef;
  }

  default <Ctx, Req, Cond>
      Consumer<Req> letDelegateStashRequestsAndStallRespondingToThemFollowingPlanInContext(
          StallingDelegatePlan<Req, Cond> stallingDelegatePlan,
          ActorContext<Ctx> context,
          BiFunction<ActorRef<StallingDelegateActor.Request>, StallingDelegatePlan<Req, Cond>, Ctx>
              generateMessageForCallerWhenDelegateTerminated) {
    Optional<ActorRef<Void>> activeDelegateMaybe =
        context.getChild(stallingDelegatePlan.delegateName());
    if (activeDelegateMaybe.isPresent()) {
      // Hack done to return an appropriately typed delegate ref e.g.
      // ActorRef<StallingDelegateBehavior.Response>
      // instead of an ActorRef<Void>
      // This hack assumes the name of the delegate is unique to the delegate at
      // the time
      // The akka api will throw an error if the client attempts to spawn a second
      // delegate with the same name
      ActorRef<StallingDelegateActor.Request> delegateActorRef =
          ActorRefAdapter.apply(ActorRefAdapter.toClassic(activeDelegateMaybe.get()));
      return req -> delegateActorRef.tell(new StallingDelegateActor.Request<Req>(req));
    }
    Behavior<StallingDelegateActor.Request> delegateBehaviorFollowingPlan =
        StallingDelegateActor.createDelegateBehaviorFollowingPlan(stallingDelegatePlan);
    ActorRef<StallingDelegateActor.Request> delegateRef =
        context.spawn(delegateBehaviorFollowingPlan, stallingDelegatePlan.delegateName());
    context.watchWith(
        delegateRef,
        generateMessageForCallerWhenDelegateTerminated.apply(delegateRef, stallingDelegatePlan));
    return req -> {
      delegateRef.tell(new StallingDelegateActor.Request<Req>(req));
    };
  }
}
