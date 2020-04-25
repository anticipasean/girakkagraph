package io.github.anticipasean.girakkagraph.protocol.graphql.command.query;

import graphql.ExecutionResult;
import org.immutables.value.Value;

@Value.Immutable
public interface GraphQlInvocationExecuted extends GraphQlQueryProtocol<GraphQlInvocationExecuted> {
  ExecutionResult executionResult();
}
