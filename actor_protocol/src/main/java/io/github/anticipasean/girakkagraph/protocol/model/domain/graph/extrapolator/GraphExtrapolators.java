package io.github.anticipasean.girakkagraph.protocol.model.domain.graph.extrapolator;

import akka.actor.typed.javadsl.ActorContext;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.BaseModelGraph;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.extrapolator.base.BaseModelGraphExtrapolator;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.extrapolator.query.QueryGraphExtrapolator;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.ModelLookUpCriteriaHashable;
import io.github.anticipasean.girakkagraph.protocol.model.domain.metamodel.MetaModelDatabase;
import javax.persistence.EntityManager;

public interface GraphExtrapolators {

  static BaseModelGraphExtrapolator
      newBaseModelGraphExtrapolatorUsingMetaModelDatabaseModelLookUpHashableAndActorContext(
          MetaModelDatabase metaModelDatabase,
          ModelLookUpCriteriaHashable modelLookUpCriteriaHashable,
          ActorContext<Command> context) {
    return BaseModelGraphExtrapolator
        .newBaseModelGraphExtrapolatorUsingMetaModelDatabaseModelLookUpHashableAndActorContext(
            metaModelDatabase, modelLookUpCriteriaHashable, context);
  }

  static QueryGraphExtrapolator
      newQueryGraphExtrapolatorUsingEntityManagerBaseModelGraphAndActorContext(
          EntityManager entityManager,
          BaseModelGraph baseModelGraph,
          ActorContext<Command> context) {
    return QueryGraphExtrapolator.newInstanceWithEntityManagerBaseModelGraphAndActorContext(
        entityManager, baseModelGraph, context);
  }
}
