package io.github.anticipasean.girakkagraph.protocol.model.domain.graph.processor;

import io.github.anticipasean.girakkagraph.protocol.model.domain.context.ProcessingContext;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.ModelGraphComponent;

public interface ModelGraphComponentProcessor<
    M extends ModelGraphComponent, C extends ProcessingContext> {

  C updateContextAccordingToComponent(C context, M component);
}
