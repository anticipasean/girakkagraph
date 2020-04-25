package io.github.anticipasean.girakkagraph.protocol.base.util.interception;

import akka.actor.typed.Behavior;

public interface InterceptorConfigurable<Ctx> {

  default <Res extends Ctx, TimO extends Ctx> Behavior<Ctx> executeInterceptorPlan(
      InterceptorPlan<Ctx, Res, TimO> interceptorPlan, Behavior<Ctx> interceptedBehavior) {
    return InterceptorActor.create(interceptorPlan, interceptedBehavior);
  }
}
