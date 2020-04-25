package io.github.anticipasean.girakkagraph.protocol.graphql.worker.schema;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.BehaviorCreator;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SpawnedContext;
import io.github.anticipasean.girakkagraph.protocol.base.worker.WorkerActor;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.mapping.GraphQlSchemaMappingProtocol;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.mapping.MapSelectionSetToModel;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.mapping.SelectionSetMappedToModelImpl;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.ModelLookUpCriteriaHashable;
import graphql.language.Field;

public class GraphQLSchemaMappingWorker extends WorkerActor<GraphQlSchemaMappingProtocol> {

  protected GraphQLSchemaMappingWorker(
      SpawnedContext<Command, GraphQlSchemaMappingProtocol> spawnedContext) {
    super(spawnedContext);
  }

  public static Behavior<Command> create(ActorRef<Command> parentActorRef) {
    return BehaviorCreator.create(
        parentActorRef, GraphQlSchemaMappingProtocol.class, GraphQLSchemaMappingWorker::new);
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilderWithDefaultCommandHandlers()
        .onMessage(MapSelectionSetToModel.class, this::onMapSelectionSetToModel)
        .build();
  }

  private Behavior<Command> onMapSelectionSetToModel(MapSelectionSetToModel command) {
    context.getLog().info("map_selection_set_to_model received: " + command);
    Field rootField = command.rootField();
    ModelLookUpCriteriaHashable modelLookUpCriteriaHashable =
        ModelLookUpCriteriaHashable
            .generateHashableModelLookUpCriteriaObjectFromQueryRootGraphQlFieldInContext(rootField);
    context.getLog().info("model_lookup_criteria_hashable: " + modelLookUpCriteriaHashable);
    replyToIfPresent(
        command,
        SelectionSetMappedToModelImpl.builder()
            .modelLookUpCriteriaHashable(modelLookUpCriteriaHashable)
            .build());
    return Behaviors.same();
  }
}
