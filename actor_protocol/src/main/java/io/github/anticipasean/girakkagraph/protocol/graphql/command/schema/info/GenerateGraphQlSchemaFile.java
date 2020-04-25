package io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.info;

import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.GraphQlSchemaProtocol;
import java.io.File;
import org.immutables.value.Value;

@Value.Immutable
public interface GenerateGraphQlSchemaFile
    extends GraphQlSchemaProtocol<GraphQlSchemaFileGenerated> {
  File graphQLSchemaToBeGeneratedFileHandle();
}
