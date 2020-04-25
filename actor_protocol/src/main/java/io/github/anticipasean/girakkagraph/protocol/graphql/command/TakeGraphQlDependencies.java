package io.github.anticipasean.girakkagraph.protocol.graphql.command;

import io.github.anticipasean.girakkagraph.protocol.graphql.dependencies.GraphQLDependencies;
import org.immutables.value.Value;

@Value.Immutable
public interface TakeGraphQlDependencies extends GraphQlProtocol<GraphQlDependenciesReceived> {
  GraphQLDependencies graphQlDependencies();
}
