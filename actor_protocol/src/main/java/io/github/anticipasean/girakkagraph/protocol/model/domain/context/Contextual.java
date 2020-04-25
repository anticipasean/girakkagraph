package io.github.anticipasean.girakkagraph.protocol.model.domain.context;

public interface Contextual<P extends ProcessingContext> {

  P processingContext();
}
