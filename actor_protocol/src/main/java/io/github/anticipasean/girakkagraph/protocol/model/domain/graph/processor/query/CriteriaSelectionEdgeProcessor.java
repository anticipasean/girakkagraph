package io.github.anticipasean.girakkagraph.protocol.model.domain.graph.processor.query;

import io.github.anticipasean.girakkagraph.protocol.model.domain.context.graph.QueryGraphOperatorProcessingContext;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.processor.ModelGraphComponentProcessor;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.vertex.BasicAttributeVertex;

public interface CriteriaSelectionEdgeProcessor
    extends ModelGraphComponentProcessor<
        BasicAttributeVertex, QueryGraphOperatorProcessingContext> {}
