package io.github.anticipasean.girakkagraph.protocol.graphql.domain.directive.wiring.type;

import graphql.schema.idl.SchemaDirectiveWiring;
import java.util.function.Function;

public interface TypeWiring<T, U> extends SchemaDirectiveWiring {

  Function<T, U> wiringGenerator();

}
