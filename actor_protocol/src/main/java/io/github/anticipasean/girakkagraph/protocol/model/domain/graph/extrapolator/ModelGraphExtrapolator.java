package io.github.anticipasean.girakkagraph.protocol.model.domain.graph.extrapolator;

import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.ModelGraph;

public interface ModelGraphExtrapolator<M extends ModelGraph> {

  M extrapolateGraph();
}
