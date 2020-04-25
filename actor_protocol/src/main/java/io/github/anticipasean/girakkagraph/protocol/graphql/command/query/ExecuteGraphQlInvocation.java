package io.github.anticipasean.girakkagraph.protocol.graphql.command.query;

import graphql.ExecutionInput;
import org.immutables.value.Value;

@Value.Immutable
public interface ExecuteGraphQlInvocation extends GraphQlQueryProtocol<GraphQlInvocationExecuted> {
  ExecutionInput executionInput();
}
