package io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.setup;

import org.immutables.value.Value;

@Value.Immutable
public interface InitializeGraphQlSchemaAndWiring
    extends SetupGraphQlSchema<GraphQlSchemaAndWiringInitialized> {}
