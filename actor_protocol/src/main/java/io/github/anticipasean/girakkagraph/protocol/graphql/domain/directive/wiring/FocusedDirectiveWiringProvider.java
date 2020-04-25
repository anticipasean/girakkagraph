package io.github.anticipasean.girakkagraph.protocol.graphql.domain.directive.wiring;

import graphql.schema.GraphQLDirectiveContainer;
import graphql.schema.idl.SchemaDirectiveWiring;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import graphql.schema.idl.WiringFactory;
import java.util.Optional;
import java.util.function.Function;

public interface FocusedDirectiveWiringProvider<
        F extends FocusedDirectiveWiring<W>, W extends GraphQLDirectiveContainer>
    extends WiringFactory {

  Class<W> wiringElementType();

  default Function<SchemaDirectiveWiringEnvironment, Optional<W>>
      retrieveWiringElementFromSchemaDirectiveWiringEnvironment() {
    return environment ->
        Optional.of(environment.getElement())
            .filter(
                graphQLDirectiveContainer ->
                    wiringElementType().isAssignableFrom(graphQLDirectiveContainer.getClass()))
            .map(graphQLDirectiveContainer -> wiringElementType().cast(graphQLDirectiveContainer));
  }

  Function<W, Optional<F>> retrieveFocusedDirectiveWiringForWiringElement();

  @Override
  default boolean providesSchemaDirectiveWiring(SchemaDirectiveWiringEnvironment environment) {
    return retrieveWiringElementFromSchemaDirectiveWiringEnvironment()
        .apply(environment)
        .map(retrieveFocusedDirectiveWiringForWiringElement())
        .filter(Optional::isPresent)
        .map(Optional::get)
        .isPresent();
  }

  @Override
  default SchemaDirectiveWiring getSchemaDirectiveWiring(
      SchemaDirectiveWiringEnvironment environment) {
    return retrieveWiringElementFromSchemaDirectiveWiringEnvironment()
        .apply(environment)
        .map(retrieveFocusedDirectiveWiringForWiringElement())
        .filter(Optional::isPresent)
        .map(Optional::get)
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    String.format(
                        "call on get_schema_directive_wiring when provider does not provide "
                            + "wiring for this wiring element: %s ",
                        environment.getElement())));
  }
}
