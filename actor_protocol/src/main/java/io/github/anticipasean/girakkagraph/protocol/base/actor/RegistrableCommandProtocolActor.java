package io.github.anticipasean.girakkagraph.protocol.base.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.ReceiveBuilder;
import akka.actor.typed.receptionist.Receptionist;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.command.monitoring.IssueTimeout;
import io.github.anticipasean.girakkagraph.protocol.base.command.receptionist.UpdateSubscribedActorRefs;
import io.github.anticipasean.girakkagraph.protocol.base.util.interception.InterceptorConfigurable;
import io.github.anticipasean.girakkagraph.protocol.base.util.lookup.LookUpConfigurable;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SpawnedContext;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.ActorRefRegistrationConfigurable;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.ActorRefSubscriptionConfigurable;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.ActorRefSubscriptions;

public abstract class RegistrableCommandProtocolActor<T extends Command>
    extends CommandProtocolActor<T>
    implements ActorRefRegistrationConfigurable<T>,
        ActorRefSubscriptionConfigurable,
        LookUpConfigurable,
        InterceptorConfigurable<Command> {

  private final ActorRef<Receptionist.Listing> receptionistListingTranslator;

  protected RegistrableCommandProtocolActor(SpawnedContext<Command, T> spawnedContext) {
    super(spawnedContext);
    registerOnInitialization();
    if (subscriptions().registrables().size() > 0) {
      this.receptionistListingTranslator =
          this.context.messageAdapter(Receptionist.Listing.class, this::onListing);
      subscribeOnInitialization();
    } else {
      this.receptionistListingTranslator = null;
    }
  }

  private UpdateSubscribedActorRefs onListing(Receptionist.Listing listing) {
    return onListingReceivedForActorRefSubscriptions(listing, subscriptions());
  }

  private void registerOnInitialization() {
    registrations().registrables().stream()
        .forEach(
            registrable -> {
              this.context
                  .getSystem()
                  .receptionist()
                  .tell(
                      Receptionist.register(registrable.serviceKey(), context.getSelf().narrow()));
            });
  }

  private void subscribeOnInitialization() {
    subscriptions().registrables().stream()
        .forEach(
            registrable ->
                context
                    .getSystem()
                    .receptionist()
                    .tell(
                        Receptionist.subscribe(
                            registrable.serviceKey(), receptionistListingTranslator)));
  }

  protected Behavior<Command> onIssueTimeout(IssueTimeout command) {
    context.getLog().info("issue_timeout: " + command);
    context.getLog().error("issue timeout received but not handled; stopping actor");
    return Behaviors.stopped();
  }

  protected Behavior<Command> onUpdateSubscribedActorRefs(UpdateSubscribedActorRefs command) {
    context.getLog().info("update_actor_refs: " + command);
    context
        .getLog()
        .warn(
            "actor ref updates are being received but not handled by this actor: "
                + selfRef()
                + "\nonUpdateActorRefs should be overridden and implemented");

    return Behaviors.same();
  }

  protected ActorRefSubscriptions subscriptions() {
    return newSubscriptionsBuilder().build();
  }

  @Override
  protected ReceiveBuilder<Command> newReceiveBuilderWithDefaultCommandHandlers() {
    return super.newReceiveBuilderWithDefaultCommandHandlers()
        .onMessage(IssueTimeout.class, this::onIssueTimeout)
        .onMessage(UpdateSubscribedActorRefs.class, this::onUpdateSubscribedActorRefs);
  }
}
