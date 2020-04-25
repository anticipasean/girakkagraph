package io.github.anticipasean.girakkagraph.protocol.graphql.command;

import akka.NotUsed;
import org.immutables.value.Value;

@Value.Immutable
public interface GraphQlDependenciesReceived extends GraphQlProtocol<NotUsed> {}
