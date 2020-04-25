package io.github.anticipasean.girakkagraph.protocol.model.command.modelindex;

import akka.actor.typed.ActorRef;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.info.FetchGraphQlSchema;
import org.immutables.value.Value;

@Value.Immutable
public interface IndexEntityMetaModel extends ModelIndexService<EntityMetaModelIndexed> {

  ActorRef<FetchGraphQlSchema> fetchGraphQlSchemaActorRef();
}
