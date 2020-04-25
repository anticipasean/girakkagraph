package io.github.anticipasean.girakkagraph.protocol.model.domain.graph.processor.query;

public interface JpaCriteriaQueryProcessors {

  static CriteriaJoinEdgeProcessor joinEdgeProcessor(){
    return new CriteriaJoinEdgeProcessorImpl();
  }

  static CriteriaSelectionEdgeProcessor selectionEdgeProcessor(){
    return new CriteriaSelectionEdgeProcessorImpl();
  }

  static CriteriaBasicAttributeVertexArgumentEdgeProcessor basicAttributeArgumentEdgeProcessor(){
    return new CriteriaBasicAttributeVertexArgumentEdgeProcessorImpl();
  }

  static CriteriaTypeVertexArgumentEdgeProcessor typeArgumentEdgeProcessor(){
    return new CriteriaTypeVertexArgumentEdgeProcessorImpl();
  }



}
