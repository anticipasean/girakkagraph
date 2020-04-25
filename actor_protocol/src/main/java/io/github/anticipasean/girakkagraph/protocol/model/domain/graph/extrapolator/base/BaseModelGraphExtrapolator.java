package io.github.anticipasean.girakkagraph.protocol.model.domain.graph.extrapolator.base;

import akka.actor.typed.javadsl.ActorContext;
import io.github.anticipasean.girakkagraph.protocol.base.command.Command;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.BaseModelGraph;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.extrapolator.ModelGraphExtrapolator;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.ModelLookUpCriteriaHashable;
import io.github.anticipasean.girakkagraph.protocol.model.domain.metamodel.MetaModelDatabase;

public interface BaseModelGraphExtrapolator extends ModelGraphExtrapolator<BaseModelGraph> {

  static BaseModelGraphExtrapolator
  newBaseModelGraphExtrapolatorUsingMetaModelDatabaseModelLookUpHashableAndActorContext(
      MetaModelDatabase metaModelDatabase,
      ModelLookUpCriteriaHashable modelLookUpCriteriaHashable,
      ActorContext<Command> context) {
    return new BaseModelGraphExtrapolatorImpl(
        metaModelDatabase, modelLookUpCriteriaHashable, context);
  }
}
