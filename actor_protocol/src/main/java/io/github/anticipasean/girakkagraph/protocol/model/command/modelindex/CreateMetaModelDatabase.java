package io.github.anticipasean.girakkagraph.protocol.model.command.modelindex;

import graphql.schema.GraphQLSchema;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
public interface CreateMetaModelDatabase extends ModelIndexService<MetaModelDatabaseCreated> {
  Optional<GraphQLSchema> graphQLSchema();
}
