package io.github.anticipasean.girakkagraph.protocol.base.util.lookup;

import akka.NotUsed;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.command.lookup.shareable.LookUpShareables;
import io.github.anticipasean.girakkagraph.protocol.base.command.lookup.shareable.LookUpShareablesImpl;
import io.github.anticipasean.girakkagraph.protocol.base.util.receptionist.ReceptionistInteractive;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.Registrable;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.RegistrableImpl;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

public interface LookUpConfigurable {

  static Behavior<Command> lookUpShareableServiceRefAction(
      ActorContext<Command> context,
      Function<ActorRef<LookUpShareables>, Behavior<Command>> onLookUpShareablesRefReceived) {
    ActorRef<Receptionist.Listing> listingActorRef =
        ReceptionistInteractive.submitSearchForRegistrableInContext(
            context, lookUpShareablesRegistrable(), LookUpListingWrapper::new);
    return Behaviors.receive(Command.class)
        .onMessage(
            LookUpListingWrapper.class,
            cmd ->
                onListingReceivedForLookUpShareables(cmd, context, onLookUpShareablesRefReceived))
        .build();
  }

  static Registrable<LookUpShareables> lookUpShareablesRegistrable() {
    return RegistrableImpl.of(LookUpShareables.class);
  }

  static Behavior<Command> onListingReceivedForLookUpShareables(
      LookUpListingWrapper cmd,
      ActorContext<Command> context,
      Function<ActorRef<LookUpShareables>, Behavior<Command>> onLookUpShareablesRefReceived) {
    Receptionist.Listing listing = cmd.getListing();
    ServiceKey<?> listingKey = listing.getKey();
    ServiceKey<LookUpShareables> lookUpShareablesServiceKey =
        lookUpShareablesRegistrable().serviceKey();
    if (listingKey.equals(lookUpShareablesServiceKey)) {
      Set<ActorRef<LookUpShareables>> serviceInstances =
          listing.getServiceInstances(lookUpShareablesServiceKey);
      Optional<ActorRef<LookUpShareables>> lookUpShareablesActorRefMaybe =
          serviceInstances.stream().findFirst();
      if (lookUpShareablesActorRefMaybe.isPresent()) {
        ActorRef<LookUpShareables> lookUpShareablesActorRef = lookUpShareablesActorRefMaybe.get();
        return onLookUpShareablesRefReceived.apply(lookUpShareablesActorRef);
      }
    }
    context
        .getLog()
        .error(
            "did not retrieve LookUpShareables actor ref",
            new NoSuchElementException("actor ref not LookShareables ref"));
    return Behaviors.stopped();
  }

  static BiConsumer<ActorContext<Command>, ActorRef<LookUpShareables>>
      onLookUpShareablesRefReceived(List<ShareableQuery> shareableQueries) {
    return (context, lookUpShareablesActorRef) -> {
      lookUpShareablesActorRef.tell(
          LookUpShareablesImpl.builder()
              .addAllLookUpShareableQueries(shareableQueries)
              .replyTo(context.getSelf().narrow())
              .build());
    };
  }

  class LookUpListingWrapper implements Command<NotUsed> {
    private Receptionist.Listing listing;

    public LookUpListingWrapper(Receptionist.Listing listing) {
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
