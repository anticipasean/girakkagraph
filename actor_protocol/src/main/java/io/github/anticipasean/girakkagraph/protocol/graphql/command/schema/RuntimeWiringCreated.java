package io.github.anticipasean.girakkagraph.protocol.graphql.command.schema;

import akka.NotUsed;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.setup.SetupGraphQlSchema;
import graphql.schema.idl.RuntimeWiring;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
public interface RuntimeWiringCreated extends SetupGraphQlSchema<NotUsed> {
  Optional<RuntimeWiring> runtimeWiring();
}
