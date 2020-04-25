package io.github.anticipasean.girakkagraph.protocol.graphql.command.query;

import io.github.anticipasean.girakkagraph.protocol.graphql.command.GraphQlProtocol;
import org.immutables.value.Value;

@com.fasterxml.jackson.databind.annotation.JsonSerialize(as = SendResultsToCallerImpl.class)
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(as = SendResultsToCallerImpl.class)
@Value.Immutable
public interface SendResultsToCaller extends GraphQlProtocol<GraphQlProtocol> {
  String query();
}
