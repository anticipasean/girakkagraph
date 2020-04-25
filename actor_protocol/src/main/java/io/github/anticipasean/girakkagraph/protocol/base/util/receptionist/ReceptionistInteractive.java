package io.github.anticipasean.girakkagraph.protocol.base.util.receptionist;

import akka.NotUsed;
import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.AskPattern;
import akka.actor.typed.receptionist.Receptionist;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.command.receptionist.SearchForRegisteredActor;
import io.github.anticipasean.girakkagraph.protocol.base.command.receptionist.SearchForRegisteredActorImpl;
import io.github.anticipasean.girakkagraph.protocol.base.util.container.ActorRefValueContainer;
import io.github.anticipasean.girakkagraph.protocol.base.util.container.ActorRefValueContainerImpl;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.Registrable;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.RegistrableImpl;
import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface ReceptionistInteractive {

  static <C, R> Optional<ActorRef<R>> submitBlockingSearchForRegistrableInContext(
      Registrable<R> registrable, ActorContext<C> context) {
    return submitBlockingSearchForRegistrableInContextWithTimeout(
        registrable, context, Duration.ofSeconds(3));
  }

  static <C, R> Optional<ActorRef<R>> submitBlockingSearchForRegistrableInContextWithTimeout(
      Registrable<R> registrable, ActorContext<C> context, Duration timeoutAfter) {
    CompletionStage<Receptionist.Listing> receptionistListingFuture =
        AskPattern.ask(
            context.getSystem().receptionist(),
            ref -> Receptionist.find(registrable.serviceKey(), ref),
            timeoutAfter,
            context.getSystem().scheduler());
    Receptionist.Listing listing = null;
    try {
      listing = receptionistListingFuture.toCompletableFuture().join();
    } catch (CompletionException e) {
      return Optional.empty();
    }
    Optional<ActorRef<R>> actorRefMaybe =
        listing.getServiceInstances(registrable.serviceKey()).stream().findAny();
    return actorRefMaybe;
  }

  static <R, C>
      ActorRef<Receptionist.Listing> submitSearchForRegistrableInContextTranslatingListing(
          ActorContext<C> context,
          Registrable<R> registrable,
          BiFunction<ActorRefValueContainer<R>, Throwable, ? extends C> whenComplete) {
    Objects.requireNonNull(context, "context");
    Objects.requireNonNull(registrable, "registrable");
    ActorRef<Receptionist.Listing> listingActorRef =
        receptionistListingTranslator(context, registrable, whenComplete);
    context
        .getSystem()
        .receptionist()
        .tell(Receptionist.find(registrable.serviceKey(), listingActorRef));
    return listingActorRef;
  }

  static <R, C> ActorRef<Receptionist.Listing> submitSearchForRegistrableInContext(
      ActorContext<C> context,
      Registrable<R> registrable,
      Function<Receptionist.Listing, ? extends C> listingWrapper) {
    Objects.requireNonNull(context, "context");
    Objects.requireNonNull(registrable, "registrable");
    ActorRef<Receptionist.Listing> listingActorRef =
        receptionistListingProvider(context, registrable, listingWrapper);
    context
        .getSystem()
        .receptionist()
        .tell(Receptionist.find(registrable.serviceKey(), listingActorRef));
    return listingActorRef;
  }

  static <C, R> ActorRef<Receptionist.Listing> receptionistListingProvider(
      ActorContext<C> context,
      Registrable<R> registrable,
      Function<Receptionist.Listing, ? extends C> listingWrapper) {
    ActorRef<Receptionist.Listing> listingActorRef =
        context.messageAdapter(Receptionist.Listing.class, listingWrapper::apply);
    return listingActorRef;
  }

  static <C, R> ActorRef<Receptionist.Listing> receptionistListingTranslator(
      ActorContext<C> context,
      Registrable<R> registrable,
      BiFunction<ActorRefValueContainer<R>, Throwable, ? extends C> whenComplete) {
    ActorRef<Receptionist.Listing> listingActorRef =
        context.messageAdapter(
            Receptionist.Listing.class,
            listing -> onListingReceivedForRegistrable(listing, registrable, whenComplete));
    return listingActorRef;
  }

  static <R, C> C onListingReceivedForRegistrable(
      Receptionist.Listing listing,
      Registrable<R> registrable,
      BiFunction<ActorRefValueContainer<R>, Throwable, ? extends C> whenComplete) {
    Objects.requireNonNull(listing, "listing");
    Objects.requireNonNull(registrable, "registrable");
    if (registrable.serviceKey().equals(listing.getKey())) {
      Optional<ActorRef<R>> actorRefResultMaybe =
          listing.getServiceInstances(registrable.serviceKey()).stream().findAny();
      if (actorRefResultMaybe.isPresent()) {
        ActorRef<R> actorRefRetrieved = actorRefResultMaybe.get();
        return whenComplete.apply(
            ActorRefValueContainerImpl.<R>builder()
                .name(registrable.id())
                .type(registrable.protocolMessageType())
                .value(actorRefRetrieved)
                .build(),
            null);
      } else {
        return whenComplete.apply(
            null,
            new NoSuchElementException(
                "no actor ref was returned matching registrable: " + registrable));
      }
    }
    return whenComplete.apply(
        null,
        new IllegalArgumentException(
            "the listing received was not for registrable " + registrable));
  }

  static <R>
      BiFunction<Class<R>, String, ? extends Command> generateSearchForRegisteredActorCommand(
          ActorContext<Command> context) {
    return (replyType, id) -> {
      SearchForRegisteredActor searchForRegisteredActorCommand =
          SearchForRegisteredActorImpl.builder()
              .registrable(RegistrableImpl.<R>of(replyType))
              .replyTo(context.getSelf().narrow())
              .build();
      return searchForRegisteredActorCommand;
    };
  }

  static <R> Function<Registrable<R>, ? extends Command> generateSearchForRegisteredActorCommmand(
      ActorContext<Command> context) {
    return registrable -> {
      SearchForRegisteredActor searchForRegisteredActor =
          SearchForRegisteredActorImpl.builder()
              .registrable(registrable)
              .replyTo(context.getSelf().narrow())
              .build();
      return searchForRegisteredActor;
    };
  }

  class ListingWrapper implements Command<NotUsed> {

    private Receptionist.Listing listing;

    public ListingWrapper(Receptionist.Listing listing) {
      this.listing = listing;
    }

    @Override
    public Optional<ActorRef<NotUsed>> replyTo() {
      return Optional.empty();
    }

    @Override
    public Optional<Throwable> errorOccurred() {
      return Optional.empty();
    }

    public Receptionist.Listing getListing() {
      return listing;
    }
  }
}
