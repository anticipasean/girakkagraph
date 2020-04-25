package io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.setup;

import akka.NotUsed;
import org.immutables.value.Value;

@Value.Immutable
public interface GraphQlSchemaAndWiringInitialized extends SetupGraphQlSchema<NotUsed> {}
