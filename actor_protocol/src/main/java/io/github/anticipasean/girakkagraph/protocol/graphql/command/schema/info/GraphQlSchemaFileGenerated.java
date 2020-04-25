package io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.info;

import akka.NotUsed;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.GraphQlSchemaProtocol;
import java.io.File;
import org.immutables.value.Value;

@Value.Immutable
public interface GraphQlSchemaFileGenerated extends GraphQlSchemaProtocol<NotUsed> {
  File graphQLSchemaToBeGeneratedFileHandle();
}
