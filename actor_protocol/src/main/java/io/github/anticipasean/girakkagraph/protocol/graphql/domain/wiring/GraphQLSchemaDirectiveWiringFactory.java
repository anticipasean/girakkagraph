package io.github.anticipasean.girakkagraph.protocol.graphql.domain.wiring;

import io.github.anticipasean.girakkagraph.protocol.graphql.domain.wiring.impl.GraphQLSchemaDirectiveWiringFactoryImpl;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operator.OperatorDatabase;
import graphql.schema.idl.SchemaDirectiveWiring;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import graphql.schema.idl.WiringFactory;

public interface GraphQLSchemaDirectiveWiringFactory extends WiringFactory {

  static GraphQLSchemaDirectiveWiringFactory newInstanceUsingOperatorDatabase(
      OperatorDatabase operatorDatabase) {
    return new GraphQLSchemaDirectiveWiringFactoryImpl(operatorDatabase);
  }

  OperatorDatabase operatorDatabase();

  @Override
  SchemaDirectiveWiring getSchemaDirectiveWiring(SchemaDirectiveWiringEnvironment environment);

  @Override
  boolean providesSchemaDirectiveWiring(SchemaDirectiveWiringEnvironment environment);
}
