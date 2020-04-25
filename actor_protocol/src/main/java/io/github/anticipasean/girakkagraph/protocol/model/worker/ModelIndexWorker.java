package io.github.anticipasean.girakkagraph.protocol.model.worker;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.BehaviorCreator;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SpawnedContext;
import io.github.anticipasean.girakkagraph.protocol.base.worker.WorkerActor;
import io.github.anticipasean.girakkagraph.protocol.model.command.modelindex.LookUpModelTypeAndAttributes;
import io.github.anticipasean.girakkagraph.protocol.model.command.modelindex.ModelIndexService;
import io.github.anticipasean.girakkagraph.protocol.model.domain.metamodel.MetaModelDatabase;
import java.util.concurrent.atomic.AtomicReference;

public class ModelIndexWorker extends WorkerActor<ModelIndexService> {
  private final AtomicReference<MetaModelDatabase> metaModelDatabaseSupplier;

  protected ModelIndexWorker(SpawnedContext<Command, ModelIndexService> spawnedContext) {
    super(spawnedContext);
    metaModelDatabaseSupplier = new AtomicReference<>();
  }

  public static Behavior<Command> create(ActorRef<Command> parentActorRef) {
    return BehaviorCreator.create(parentActorRef, ModelIndexService.class, ModelIndexWorker::new);
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilderWithDefaultCommandHandlers()
        .onMessage(LookUpModelTypeAndAttributes.class, this::onLookUpModelTypeAndAttributes)
        .build();
  }

  private Behavior<Command> onLookUpModelTypeAndAttributes(
      LookUpModelTypeAndAttributes lookUpModelTypeAndAttributes) {
    context
        .getLog()
        .info("lookup_model_types_and_attributes received: " + lookUpModelTypeAndAttributes);
//    if (metaModelDatabaseSupplier.get() == null) {
//      CompletionStage<MetaModelDatabase> askReply =
//          AskPattern.ask(
//              parentRef(),
//              ref -> ProvideMetaModelDatabaseImpl.builder().replyTo(ref).build(),
//              Duration.ofSeconds(5),
//              context.getSystem().scheduler());
//      MetaModelDatabase metaModelDatabase = null;
//      try {
//        metaModelDatabase = askReply.toCompletableFuture().join();
//        metaModelDatabaseSupplier.set(metaModelDatabase);
//      } catch (Exception e) {
//        context
//            .getLog()
//            .error(
//                "an error occurred when retrieving the metamodel database needed for model type and attribute lookups",
//                e);
//        return Behaviors.stopped();
//      }
//    }
//
//    ModelPathMapper modelPathMapper = new ModelPathMapper(metaModelDatabaseSupplier.get());
//    ModelPathMap modelPathMap;
//    try {
//      modelPathMap = modelPathMapper.find(lookUpModelTypeAndAttributes.paths());
//    } catch (IllegalArgumentException e) {
//      ModelTypeAndAttributesFound modelTypeAndAttributesFound =
//          ModelTypeAndAttributesFoundImpl.builder().errorOccurred(e).build();
//      replyToIfPresent(lookUpModelTypeAndAttributes, modelTypeAndAttributesFound);
//      return Behaviors.same();
//    }
//    context
//        .getLog()
//        .info(
//            "model_path_tree: "
//                + modelPathMap.mappedNodes().keySet().stream()
//                    .map(ModelPath::uri)
//                    .map(URI::toString)
//                    .collect(Collectors.joining("\n")));
//    ModelTypeAndAttributesFound response =
//        ModelTypeAndAttributesFoundImpl.builder().modelPathMap(modelPathMap).build();
//    replyToIfPresent(lookUpModelTypeAndAttributes, response);
    return Behaviors.same();
  }

  public boolean isInitialized() {
    return metaModelDatabaseSupplier.get() != null;
  }
}
