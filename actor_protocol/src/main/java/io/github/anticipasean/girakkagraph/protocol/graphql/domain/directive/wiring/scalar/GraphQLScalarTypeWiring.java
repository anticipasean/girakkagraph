package io.github.anticipasean.girakkagraph.protocol.graphql.domain.directive.wiring.scalar;

import io.github.anticipasean.girakkagraph.protocol.graphql.domain.directive.wiring.FocusedDirectiveWiring;
import graphql.schema.GraphQLScalarType;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;

public interface GraphQLScalarTypeWiring extends FocusedDirectiveWiring<GraphQLScalarType> {

  @Override
  default GraphQLScalarType onScalar(
      SchemaDirectiveWiringEnvironment<GraphQLScalarType> environment) {
    return onWiringElementEncountered(environment.getElement());
  }

}
