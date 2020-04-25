package io.github.anticipasean.girakkagraph.protocol.graphql.command.query;

import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.setup.DataFetcherFactoryProvided;
import graphql.schema.idl.FieldWiringEnvironment;
import org.immutables.value.Value;

@Value.Immutable
public interface ProvideDataFetcherFactory
    extends GraphQlQueryProtocol<DataFetcherFactoryProvided> {
  FieldWiringEnvironment fieldWiringEnvironment();
}
