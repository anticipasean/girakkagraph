package io.github.anticipasean.girakkagraph.protocol.model.domain.graph.processor.query;

import io.github.anticipasean.girakkagraph.protocol.model.domain.context.graph.QueryGraphOperatorProcessingContext;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.UnprocessedArgumentEdge;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.processor.ModelGraphComponentProcessor;

public interface CriteriaBasicAttributeVertexArgumentEdgeProcessor
    extends ModelGraphComponentProcessor<
        UnprocessedArgumentEdge, QueryGraphOperatorProcessingContext> {}
