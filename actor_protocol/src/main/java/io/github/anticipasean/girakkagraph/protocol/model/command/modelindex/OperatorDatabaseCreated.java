package io.github.anticipasean.girakkagraph.protocol.model.command.modelindex;

import akka.NotUsed;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operator.OperatorDatabase;
import org.immutables.value.Value;

@Value.Immutable
public interface OperatorDatabaseCreated extends ModelIndexService<NotUsed> {
  OperatorDatabase operatorDatabase();
}
