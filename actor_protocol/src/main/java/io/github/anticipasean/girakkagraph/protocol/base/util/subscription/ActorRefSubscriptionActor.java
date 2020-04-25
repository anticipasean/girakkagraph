package io.github.anticipasean.girakkagraph.protocol.base.util.subscription;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import io.github.anticipasean.girakkagraph.protocol.base.actor.BaseActor;
import io.github.anticipasean.girakkagraph.protocol.base.role.Role;
import io.github.anticipasean.girakkagraph.protocol.base.role.Roles;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SubordinateSpawner;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

public class ActorRefSubscriptionActor<T> extends BaseActor<Receptionist.Listing> {

  private final AtomicReference<ActorRef<T>> actorRefHolder;
  private final ServiceKey<T> serviceKey;
  private final BiFunction<ServiceKey<T>, Throwable, Behavior<Receptionist.Listing>> onFailure;

  protected ActorRefSubscriptionActor(
      ActorContext<Receptionist.Listing> context,
      ServiceKey<T> serviceKey,
      BiFunction<ServiceKey<T>, Throwable, Behavior<Receptionist.Listing>> onFailure) {
    super(context);
    this.actorRefHolder = new AtomicReference<>();
    this.serviceKey = serviceKey;
    this.onFailure = onFailure;
    this.context
        .getLog()
        .info(
            "placing call to receptionist for initial serviceActorRef for serviceKey: "
                + serviceKey);
    this.context
        .getSystem()
        .receptionist()
        .tell(Receptionist.subscribe(serviceKey, context.getSelf()));
  }

  public static <C> Behavior<Receptionist.Listing> create(
      ServiceKey<C> serviceKey,
      BiFunction<ServiceKey<C>, Throwable, Behavior<Receptionist.Listing>> onFailure) {

    return Behaviors.setup(
        (ActorContext<Receptionist.Listing> context) -> {
          return new ActorRefSubscriptionActor<C>(context, serviceKey, onFailure);
        });
  }

  @Override
  public Role role() {
    return Roles.PROTOCOL_HANDLER;
  }

  @Override
  public Receive<Receptionist.Listing> createReceive() {
    return newReceiveBuilder()
        .onMessage(Receptionist.Listing.class, this::onListingReceived)
        .build();
  }

  private Behavior<Receptionist.Listing> onListingReceived(Receptionist.Listing listing) {
    Set<ActorRef<T>> serviceInstances = listing.getServiceInstances(serviceKey);
    context
        .getLog()
        .info(
            "received listings: "
                + serviceInstances
                + " for holder subscription to: "
                + serviceKey);
    if (holderNotYetPopulatedWithServiceActorRef()) {
      return populateHolderWithServiceActorRef(serviceInstances);
    }
    if (holderAlreadyContainsRefToActiveService(serviceInstances)) {
      return Behaviors.same();
    }
    return updateHolderWithActiveServiceRef(serviceInstances);
  }

  private Behavior<Receptionist.Listing> updateHolderWithActiveServiceRef(
      Set<ActorRef<T>> serviceInstances) {
    Optional<ActorRef<T>> serviceRefMaybe = serviceInstances.stream().findFirst();
    if (serviceRefMaybe.isPresent()) {
      actorRefHolder.set(serviceRefMaybe.get());
      return Behaviors.same();
    }
    return onFailure.apply(
        serviceKey,
        new IllegalStateException(
            String.format(
                "service ref for service key [ %s ] no longer active or registered. cannot query",
                serviceKey)));
  }

  private boolean holderAlreadyContainsRefToActiveService(Set<ActorRef<T>> serviceInstances) {
    return serviceInstances.contains(actorRefHolder.get());
  }

  private Behavior<Receptionist.Listing> populateHolderWithServiceActorRef(
      Set<ActorRef<T>> serviceInstances) {
    Optional<ActorRef<T>> serviceActorRefMaybe = serviceInstances.stream().findFirst();
    if (serviceActorRefMaybe.isPresent()) {
      actorRefHolder.set(serviceActorRefMaybe.get());
      return Behaviors.same();
    } else {
      return onFailure.apply(
          serviceKey,
          new IllegalStateException(
              String.format(
                  "service ref for service key [ %s ] not active or registered. cannot query",
                  serviceKey)));
    }
  }

  private boolean holderNotYetPopulatedWithServiceActorRef() {
    return actorRefHolder.get() == null;
  }

  public AtomicReference<ActorRef<T>> getActorRefHolder() {
    return actorRefHolder;
  }

  @Override
  protected SubordinateSpawner<Receptionist.Listing> spawnSubordinatesOnContextCreation() {
    return newSubordinateSpawner();
  }
}
