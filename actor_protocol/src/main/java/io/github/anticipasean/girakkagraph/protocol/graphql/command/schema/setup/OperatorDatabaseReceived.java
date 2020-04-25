package io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.setup;

import akka.NotUsed;
import io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.GraphQlSchemaProtocol;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operator.OperatorDatabase;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
public interface OperatorDatabaseReceived extends GraphQlSchemaProtocol<NotUsed> {
  Optional<OperatorDatabase> operatorDatabase();
}
