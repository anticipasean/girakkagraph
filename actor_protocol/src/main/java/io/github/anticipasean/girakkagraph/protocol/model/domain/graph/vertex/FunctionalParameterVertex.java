package io.github.anticipasean.girakkagraph.protocol.model.domain.graph.vertex;

import org.immutables.value.Value;

@Value.Immutable
public interface FunctionalParameterVertex extends ParameterVertex {

  Class<?> functionalParameterType();

}
