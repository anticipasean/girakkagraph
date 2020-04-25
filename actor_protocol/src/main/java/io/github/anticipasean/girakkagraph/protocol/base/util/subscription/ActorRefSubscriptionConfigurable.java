package io.github.anticipasean.girakkagraph.protocol.base.util.subscription;

import akka.actor.typed.ActorRef;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import io.github.anticipasean.girakkagraph.protocol.base.command.receptionist.UpdateSubscribedActorRefs;
import io.github.anticipasean.girakkagraph.protocol.base.command.receptionist.UpdateSubscribedActorRefsImpl;
import io.github.anticipasean.girakkagraph.protocol.base.util.container.ActorRefValueContainer;
import io.github.anticipasean.girakkagraph.protocol.base.util.container.ValueContainer;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public interface ActorRefSubscriptionConfigurable {

  default ActorRefSubscriptionsImpl.Builder newSubscriptionsBuilder() {
    return ActorRefSubscriptionsImpl.builder();
  }

  default <R> void updateActorRefHolderOnUpdateSubscribedActorRefsIfApplicableToRegistrable(
      UpdateSubscribedActorRefs command,
      Registrable<R> registrable,
      AtomicReference<ActorRef<R>> refHolder) {
    if (command.errorOccurred().isPresent()) {
      return;
    }
    if (!command.listingReceived().isForKey(registrable.serviceKey())) {
      return;
    }
    Set<ActorRef<R>> serviceInstances =
        command.listingReceived().getServiceInstances(registrable.serviceKey());
    Optional<ActorRef<R>> anyUpdatedRef = serviceInstances.stream().findAny();
    anyUpdatedRef.ifPresent(refHolder::set);
  }

  default UpdateSubscribedActorRefs onListingReceivedForActorRefSubscriptions(
      Receptionist.Listing listing, ActorRefSubscriptions subscriptions) {
    ServiceKey<?> listingKey = listing.getKey();
    UpdateSubscribedActorRefs updateSubscribedActorRefs = null;
    if (subscriptions.registrables().stream()
        .noneMatch(registrable -> registrable.serviceKey().equals(listingKey))) {
      updateSubscribedActorRefs =
          UpdateSubscribedActorRefsImpl.builder()
              .errorOccurred(
                  new IllegalArgumentException(
                      "none of the subscription service keys for actor [ %s ] match that of the listing received"))
              .listingReceived(listing)
              .build();
    } else {
      updateSubscribedActorRefs =
          UpdateSubscribedActorRefsImpl.builder().listingReceived(listing).build();
    }
    return updateSubscribedActorRefs;
  }

  default <R>
      Optional<ActorRefValueContainer<R>> actorRefContainerOfOneRefInListingIfRegistrableMatches(
          Registrable<R> registrable, Receptionist.Listing listing) {
    ServiceKey<R> serviceKeyForRegisteredType =
        ServiceKey.create(registrable.protocolMessageType(), registrable.id());
    Optional<ActorRef<R>> registeredActorRefMaybe =
        listing.getServiceInstances(serviceKeyForRegisteredType).stream().findAny();
    if (registeredActorRefMaybe.isPresent()) {
      ActorRef<R> registeredActorRef = registeredActorRefMaybe.get();
      return Optional.of(
          ValueContainer.buildContainer(registeredActorRef, registrable.protocolMessageType()));
    }
    return Optional.empty();
  }

  //  default <T>
  //      BiFunction<ServiceKey<T>, Throwable, Behavior<Receptionist.Listing>>
  // onSubscriptionFailure() {
  //    return (serviceKey, throwable) -> {
  //      ReceiveSubscriptionFeedback receiveSubscriptionFeedback =
  //          ReceiveSubscriptionFeedbackImpl.builder()
  //              .errorOccurred(throwable)
  //              .subscribedToUpdatesForRefs(serviceKey)
  //              .build();
  //      subscriberRef().tell(receiveSubscriptionFeedback);
  //      return Behaviors.stopped();
  //    };
  //  }

}
/*

         .orElseThrow(
             () ->
                 new UnexpectedTypeException(
                     "the receive_actor_ref_updates command "
                         + command
                         + " does not contain an actor ref for registered type of "
                         + subscribedCommandType.getName()
                         + ", but rather "
                         + command.updatedActorRefInContainer().type().getName()));
*/
