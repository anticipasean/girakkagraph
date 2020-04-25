package io.github.anticipasean.girakkagraph.protocol.graphql.command.schema;

import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.setup.SetupGraphQlSchema;
import graphql.schema.idl.WiringFactory;
import org.immutables.value.Value;

@Value.Immutable
public interface CreateRuntimeWiring extends SetupGraphQlSchema<RuntimeWiringCreated> {

  WiringFactory wiringFactory();
}
