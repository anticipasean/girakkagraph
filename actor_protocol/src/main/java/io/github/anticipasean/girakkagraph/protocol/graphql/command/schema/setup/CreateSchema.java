package io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.setup;

import graphql.schema.idl.WiringFactory;
import org.immutables.value.Value;

@Value.Immutable
public interface CreateSchema extends SetupGraphQlSchema<SchemaCreated> {

  WiringFactory wiringFactory();
}
