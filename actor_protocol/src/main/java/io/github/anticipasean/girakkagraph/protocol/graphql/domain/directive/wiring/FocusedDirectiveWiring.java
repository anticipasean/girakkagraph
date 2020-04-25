package io.github.anticipasean.girakkagraph.protocol.graphql.domain.directive.wiring;

import graphql.schema.GraphQLDirectiveContainer;
import graphql.schema.idl.SchemaDirectiveWiring;
import java.util.function.Predicate;

public interface FocusedDirectiveWiring<W extends GraphQLDirectiveContainer> extends
    SchemaDirectiveWiring {

  Predicate<W> shouldApplyDirectiveToWiringElement();

  W onWiringElementEncountered(W wiringElement);

}
