package io.github.anticipasean.girakkagraph.protocol.base.util.routing;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.PoolRouter;
import akka.actor.typed.javadsl.Routers;
import java.util.Optional;
import java.util.function.Function;

public interface RouterConfigurable<C> {

  default ActorRef<C> spawnRouter(
      ActorContext<C> context,
      String routerName,
      Function<ActorRef<C>, Behavior<C>> createWorkerBehaviorWithIdAndParentServiceRef,
      Optional<String> pathToRouterConfig) {
    int poolSize = 1;
    String routingScheme = "round-robin-pool";
    if (pathToRouterConfig.isPresent()) {
      int pathLen = pathToRouterConfig.get().length();
      StringBuilder pathBuilder = new StringBuilder(pathToRouterConfig.get());
      boolean hasConfiguredPoolSize =
          context
              .getSystem()
              .settings()
              .config()
              .hasPath(pathBuilder.append(".pool-size").toString());
      if (hasConfiguredPoolSize) {
        poolSize = context.getSystem().settings().config().getInt(pathBuilder.toString());
      }
      pathBuilder.setLength(pathLen);
      boolean hasConfiguredRoutingScheme =
          context.getSystem().settings().config().hasPath(pathBuilder.append(".router").toString());
      if (hasConfiguredRoutingScheme) {
        routingScheme = context.getSystem().settings().config().getString(pathBuilder.toString());
      }
      pathBuilder.setLength(pathLen);
    }
    PoolRouter<C> router = null;
    Function<ActorRef<C>, Behavior<C>> createWorkerBehaviorWithSupervision =
        addSupervisionToWorkers(createWorkerBehaviorWithIdAndParentServiceRef);
    String routerId = routerName + "-worker-pool";
    if (routingScheme.equals("round-robin-pool")) {
      router =
          Routers.pool(poolSize, createWorkerBehaviorWithSupervision.apply(context.getSelf()))
              .withRoundRobinRouting();
    } else if (routingScheme.equals("random-pool")) {
      router =
          Routers.pool(poolSize, createWorkerBehaviorWithSupervision.apply(context.getSelf()))
              .withRandomRouting();
    } else {
      router =
          Routers.pool(poolSize, createWorkerBehaviorWithSupervision.apply(context.getSelf()))
              .withRoundRobinRouting();
    }
    return context.spawn(router, routerId);
  }

  default Function<ActorRef<C>, Behavior<C>> addSupervisionToWorkers(
      Function<ActorRef<C>, Behavior<C>> createWorkerBehaviorWithParentServiceRef) {
    return createWorkerBehaviorWithParentServiceRef.andThen(
        workerBehavior ->
            Behaviors.supervise(workerBehavior).onFailure(SupervisorStrategy.restart()));
  }
}
