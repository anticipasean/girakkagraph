package io.github.anticipasean.girakkagraph.protocol.graphql.worker.query;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.japi.Pair;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.base.util.receptionist.ReceptionistInteractive;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.BehaviorCreator;
import io.github.anticipasean.girakkagraph.protocol.base.util.spawn.SpawnedContext;
import io.github.anticipasean.girakkagraph.protocol.base.util.subscription.RegistrableImpl;
import io.github.anticipasean.girakkagraph.protocol.base.worker.WorkerActor;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.query.GraphQlQueriedImpl;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.query.QueryGraphQl;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.mapping.MapSelectionSetToModel;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.mapping.MapSelectionSetToModelImpl;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.mapping.SelectionSetMappedToModel;
import io.github.anticipasean.girakkagraph.protocol.graphql.tracker.GraphQLQueryTracker;
import io.github.anticipasean.girakkagraph.protocol.model.command.modelindex.LookUpModelTypeAndAttributes;
import io.github.anticipasean.girakkagraph.protocol.model.command.modelindex.LookUpModelTypeAndAttributesImpl;
import io.github.anticipasean.girakkagraph.protocol.model.command.modelindex.ModelTypeAndAttributesFound;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.ModelLookUpCriteriaHashable;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.ModelPath;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLOutputType;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class QueryGraphQLWorker extends WorkerActor<QueryGraphQl> {

  private AtomicReference<ActorRef<MapSelectionSetToModel>> selectionMapperActorRefHolder;
  private AtomicReference<ActorRef<LookUpModelTypeAndAttributes>>
      lookUpModelTypeAndAttributesActorRefHolder;

  public QueryGraphQLWorker(SpawnedContext<Command, QueryGraphQl> spawnedContext) {
    super(spawnedContext);
    selectionMapperActorRefHolder = new AtomicReference<>();
    lookUpModelTypeAndAttributesActorRefHolder = new AtomicReference<>();
  }

  public static Behavior<Command> create(ActorRef<Command> parentActorRef) {
    return BehaviorCreator.create(parentActorRef, QueryGraphQl.class, QueryGraphQLWorker::new);
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilderWithDefaultCommandHandlers()
        .onMessage(QueryGraphQl.class, this::onQueryGraphQL)
        .onMessage(SelectionSetMappedToModel.class, this::onSelectionSetMappedToModel)
        .onMessage(ModelTypeAndAttributesFound.class, this::onModelTypeAndAttributesFound)
        .build();
  }

  private Behavior<Command> onModelTypeAndAttributesFound(
      ModelTypeAndAttributesFound modelTypeAndAttributesFound) {
    context
        .getLog()
        .info("model_type_and_attributes_found received: " + modelTypeAndAttributesFound);

    return Behaviors.same();
  }

  private Behavior<Command> onSelectionSetMappedToModel(SelectionSetMappedToModel command) {
    context.getLog().info("selection_set_mapped_to_model received: " + command);
    Optional<ModelLookUpCriteriaHashable> modelLookUpCriteriaHashable =
        command.modelLookUpCriteriaHashable();
    if (modelLookUpCriteriaHashable.isPresent()) {
      context.getLog().info("model_lookup_hashable received: " + modelLookUpCriteriaHashable);
      List<ModelPath> pathsFromHashable =
          ModelLookUpCriteriaHashable.pathsFromHashable(modelLookUpCriteriaHashable.get());
      context
          .getLog()
          .info(
              "paths: [ "
                  + pathsFromHashable.stream()
                      .map(modelPath -> modelPath.toString())
                      .collect(Collectors.joining(", "))
                  + " ]");
      LookUpModelTypeAndAttributes lookUpModelTypeAndAttributes =
          LookUpModelTypeAndAttributesImpl.builder()
              .addAllPaths(pathsFromHashable)
              .replyTo(selfRef().narrow())
              .build();
      if (lookUpModelTypeAndAttributesActorRefHolder.get() == null) {
        Optional<ActorRef<LookUpModelTypeAndAttributes>> lookUpModelTypeAndAttributesActorRef =
            ReceptionistInteractive.submitBlockingSearchForRegistrableInContextWithTimeout(
                RegistrableImpl.of(LookUpModelTypeAndAttributes.class),
                context,
                Duration.ofSeconds(8));
        lookUpModelTypeAndAttributesActorRefHolder.set(lookUpModelTypeAndAttributesActorRef.get());
      }
      lookUpModelTypeAndAttributesActorRefHolder.get().tell(lookUpModelTypeAndAttributes);
    }
    return Behaviors.same();
  }

  private Behavior<Command> onQueryGraphQL(QueryGraphQl command) {
    context.getLog().info("query_graphql received: " + command);
    if (lookUpModelTypeAndAttributesActorRefHolder.get() == null) {
      int attempts = 3;
      while (lookUpModelTypeAndAttributesActorRefHolder.get() == null && attempts > 0) {
        Optional<ActorRef<LookUpModelTypeAndAttributes>> lookUpModelTypeAndAttributesActorRef =
            ReceptionistInteractive.submitBlockingSearchForRegistrableInContextWithTimeout(
                RegistrableImpl.of(LookUpModelTypeAndAttributes.class),
                context,
                Duration.ofSeconds(8));
        if (lookUpModelTypeAndAttributesActorRef.isPresent()) {
          lookUpModelTypeAndAttributesActorRefHolder.set(
              lookUpModelTypeAndAttributesActorRef.get());
        } else {
          attempts--;
        }
      }
    }
    if (selectionMapperActorRefHolder.get() == null) {
      Optional<ActorRef<MapSelectionSetToModel>> selectionSetToModelActorRefMaybe =
          ReceptionistInteractive.submitBlockingSearchForRegistrableInContext(
              RegistrableImpl.of(MapSelectionSetToModel.class), context);
      selectionMapperActorRefHolder.set(selectionSetToModelActorRefMaybe.get());
    }
    if (command.commandId() != null) {
      ActorRef<Command> tracker =
          context.spawnAnonymous(
              GraphQLQueryTracker.create(
                  command.commandId(),
                  selectionMapperActorRefHolder.get(),
                  lookUpModelTypeAndAttributesActorRefHolder.get()));
      tracker.tell(command);
      return Behaviors.same();
    }

    DataFetchingEnvironment env = command.dataFetchingEnvironment();
    context.getLog().info("field: " + env.getField());
    if (isFetchingRootField(env)) {
      context.getLog().info("is_fetching_root_field: true");
      String rootFieldName = env.getField().getName();
      GraphQLOutputType rootFieldType = env.getFieldType();
      Map<String, GraphQLOutputType> qualifiedFieldNameToGraphQLOutputTypeMap =
          env.getSelectionSet().getFields().stream()
              .parallel()
              .map(
                  selectedField ->
                      Pair.create(
                          selectedField.getQualifiedName(),
                          selectedField.getFieldDefinition().getType()))
              .collect(Collectors.toMap(Pair::first, Pair::second));
      MapSelectionSetToModel mapSelectionSetToModel =
          MapSelectionSetToModelImpl.builder()
              .dataFetchingFieldSelectionSet(env.getSelectionSet())
              .rootField(env.getField())
              .rootFieldType(env.getFieldType())
              .replyTo(context.getSelf().narrow())
              .build();
      if (selectionMapperActorRefHolder.get() == null) {
        Optional<ActorRef<MapSelectionSetToModel>> selectionSetToModelActorRefMaybe =
            ReceptionistInteractive.submitBlockingSearchForRegistrableInContext(
                RegistrableImpl.of(MapSelectionSetToModel.class), context);
        selectionMapperActorRefHolder.set(selectionSetToModelActorRefMaybe.get());
      }
      selectionMapperActorRefHolder.get().tell(mapSelectionSetToModel);
    } else {
      context.getLog().info("is_fetching_root_field: false");
    }

    replyToIfPresent(
        command,
        GraphQlQueriedImpl.builder()
            .commandId(command.commandId())
            .dataFetcherResult(
                DataFetcherResult.newResult().data(env.getField().getName() + "_blah").build())
            .build());
    return Behaviors.same();
  }

  private boolean isFetchingRootField(DataFetchingEnvironment env) {
    return env.getSource() == null;
  }
}
