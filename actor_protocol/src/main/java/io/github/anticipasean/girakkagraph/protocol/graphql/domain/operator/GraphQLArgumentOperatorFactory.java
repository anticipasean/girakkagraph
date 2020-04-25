package io.github.anticipasean.girakkagraph.protocol.graphql.domain.operator;

import io.github.anticipasean.girakkagraph.protocol.graphql.domain.operator.impl.GraphQLArgumentOperatorFactoryImpl;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operator.OperatorDatabase;
import java.util.Set;

public interface GraphQLArgumentOperatorFactory {

  OperatorDatabase operatorDatabase();

  Set<GraphQLArgumentOperator> createGraphQLArgumentOperatorSetFromOperatorDatabase();

  static GraphQLArgumentOperatorFactory newInstanceWithOperatorDatabase(OperatorDatabase operatorDatabase){
    return new GraphQLArgumentOperatorFactoryImpl(operatorDatabase);
  }

}
