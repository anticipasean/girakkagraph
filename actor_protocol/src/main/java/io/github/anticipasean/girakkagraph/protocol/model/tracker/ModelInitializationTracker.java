package io.github.anticipasean.girakkagraph.protocol.model.tracker;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.command.lookup.shareable.LookUpShareableResultsFound;
import io.github.anticipasean.girakkagraph.protocol.base.command.lookup.shareable.LookUpShareablesImpl;
import io.github.anticipasean.girakkagraph.protocol.base.util.lookup.LookUpConfigurable;
import io.github.anticipasean.girakkagraph.protocol.base.util.lookup.ShareableQuery;
import io.github.anticipasean.girakkagraph.protocol.base.util.lookup.ShareableQueryImpl;
import io.github.anticipasean.girakkagraph.protocol.base.util.receptionist.ReceptionistInteractive;
import io.github.anticipasean.girakkagraph.protocol.base.util.shareable.Shareable;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.Registrable;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.RegistrableImpl;
import io.github.anticipasean.girakkagraph.protocol.model.command.modelindex.ModelIndexService;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.persistence.EntityManager;

public class ModelInitializationTracker {

  public static Behavior<Command> startInitialization() {
    return Behaviors.setup(
        context -> {
          List<ShareableQuery> shareableQueries =
              Arrays.asList(
                  ShareableQueryImpl.of(
                      "entityManager", AtomicReference.class, EntityManager.class));
          return LookUpConfigurable.lookUpShareableServiceRefAction(
              context,
              lookUpShareablesActorRef -> {
                lookUpShareablesActorRef.tell(
                    LookUpShareablesImpl.builder()
                        .addLookUpShareableQuery(
                            ShareableQueryImpl.of(
                                "entityManager", AtomicReference.class, EntityManager.class))
                        .replyTo(context.getSelf().narrow())
                        .build());
                return onEntityManagerShareableReceived(context);
              });
        });
  }

  private static Behavior<Command> onEntityManagerShareableReceived(ActorContext<Command> context) {
    return Behaviors.receive(Command.class)
        .onMessage(
            LookUpShareableResultsFound.class,
            lookUpShareableResultsFound -> {
              Optional<Shareable> shareableMaybe =
                  lookUpShareableResultsFound.shareablesFound().stream().findFirst();
              if (shareableMaybe.isPresent()) {
                Shareable shareable = shareableMaybe.get();
                if (shareable.valueContainer().value() instanceof EntityManager) {
                  EntityManager entityManager = (EntityManager) shareable.valueContainer().value();
                  ActorRef<Receptionist.Listing> listingActorRef =
                      ReceptionistInteractive.submitSearchForRegistrableInContext(
                          context,
                          modelIndexServiceRegistrable(),
                          ReceptionistInteractive.ListingWrapper::new);
                  return onModelIndexServiceRefReceived(context, entityManager);
                }
              }
              return Behaviors.stopped();
            })
        .build();
  }

  private static Registrable<ModelIndexService> modelIndexServiceRegistrable() {
    return RegistrableImpl.of(ModelIndexService.class);
  }

  private static Behavior<Command> onModelIndexServiceRefReceived(
      ActorContext<Command> context, EntityManager entityManager) {
    return Behaviors.receive(Command.class)
        .onMessage(
            ReceptionistInteractive.ListingWrapper.class,
            cmd -> {
              Receptionist.Listing listing = cmd.getListing();
              ServiceKey<ModelIndexService> modelIndexCommandServiceKey =
                  modelIndexServiceRegistrable().serviceKey();
              Set<ActorRef<ModelIndexService>> serviceInstances =
                  listing.getServiceInstances(modelIndexCommandServiceKey);
              Optional<ActorRef<ModelIndexService>> modelIndexCommandActorRefMaybe =
                  serviceInstances.stream().findFirst();
              if (modelIndexCommandActorRefMaybe.isPresent()) {
                ActorRef<ModelIndexService> modelIndexServiceRef =
                    modelIndexCommandActorRefMaybe.get();
                //                modelIndexServiceRef.tell(
                //                    TakeEntityManagerImpl.builder()
                //                        .entityManager(entityManager)
                //                        .replyTo(context.getSelf().narrow())
                //                        .build());
                //                return onEntityManagerReceivedByModelIndexService(context);
              }
              return Behaviors.stopped();
            })
        .build();
  }

  //    private static Behavior<Command>
  // onEntityManagerReceivedByModelIndexService(ActorContext<Command> context) {
  //        return Behaviors.receive(Command.class)
  //            .onMessage(
  //                EntityManagerReceived.class,
  //                entityManagerReceived -> {
  //                  if (entityManagerReceived.errorOccurred().isPresent()) {
  //                    context
  //                        .getLog()
  //                        .error(
  //                            "an error occurred in transferring the entity manager to the model
  // index service",
  //                            entityManagerReceived.errorOccurred().get());
  //                  }
  //                  return Behaviors.stopped();
  //                })
  //            .build();
  //    }
}

/*

                            return Behaviors.receive(Command.class).onMessage(EntityManagerReceived.class, cmd -> {
                                if (cmd.errorOccurred().isPresent()) {
                                    String message = "an error occurred when sending the entity manager to the modex index service";
                                    context.getLog().error(message, cmd.errorOccurred().get());
                                }
                            });

                                                    .onMessage(LookUpShareableResultsFound.class, lookUpShareableResultsFound -> {
                    Optional<Shareable> shareableMaybe = lookUpShareableResultsFound.shareablesFound().stream().findFirst();
                    if (shareableMaybe.isPresent()) {
                        Shareable shareable = shareableMaybe.get();
                        if (shareable.valueContainer().value() instanceof EntityManager){
                            EntityManager entityManager = (EntityManager) shareable.valueContainer().value();

                        }
                    }
                }).build();
*/
