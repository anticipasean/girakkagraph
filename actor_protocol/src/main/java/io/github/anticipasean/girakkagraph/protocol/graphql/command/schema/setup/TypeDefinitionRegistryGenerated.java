package io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.setup;

import akka.NotUsed;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
public interface TypeDefinitionRegistryGenerated extends SetupGraphQlSchema<NotUsed> {
  Optional<TypeDefinitionRegistry> typeDefinitionRegistry();
}
