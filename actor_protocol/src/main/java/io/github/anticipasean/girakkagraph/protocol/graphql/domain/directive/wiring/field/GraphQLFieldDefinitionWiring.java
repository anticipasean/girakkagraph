package io.github.anticipasean.girakkagraph.protocol.graphql.domain.directive.wiring.field;

import io.github.anticipasean.girakkagraph.protocol.graphql.domain.directive.wiring.FocusedDirectiveWiring;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import java.util.function.Predicate;

public interface GraphQLFieldDefinitionWiring extends
    FocusedDirectiveWiring<GraphQLFieldDefinition> {

  @Override
  default GraphQLFieldDefinition onField(
      SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment){
    return onWiringElementEncountered(environment.getFieldDefinition());
  }

  @Override
  Predicate<GraphQLFieldDefinition> shouldApplyDirectiveToWiringElement();

  @Override
  GraphQLFieldDefinition onWiringElementEncountered(GraphQLFieldDefinition graphQLFieldDefinition);
}
