package io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.setup;

import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.GraphQlSchemaProtocol;
import graphql.schema.idl.WiringFactory;
import org.immutables.value.Value;

@Value.Immutable
public interface ProvideWiringFactory extends GraphQlSchemaProtocol<WiringFactory> {}
