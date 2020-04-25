package io.github.anticipasean.girakkagraph.protocol.model.domain.graph.vertex;

import org.immutables.value.Value.Immutable;

@Immutable
public interface RepresentativeParameterVertex extends ParameterVertex {

  Class<?> representativeType();

}
