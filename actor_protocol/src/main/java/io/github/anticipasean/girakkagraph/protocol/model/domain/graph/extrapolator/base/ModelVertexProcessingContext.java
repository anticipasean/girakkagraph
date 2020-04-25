package io.github.anticipasean.girakkagraph.protocol.model.domain.graph.extrapolator.base;

import io.github.anticipasean.girakkagraph.protocol.model.domain.context.ProcessingContext;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.ModelVertex;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.ModelPath;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import org.immutables.value.Value;

/**
 * This class is used to retain the vertex instances for parents of the incoming unprocessed model
 * graph components so that their child vertices may be mapped properly. The grandparents for each
 * depth of the tree are removed so that the sorting and copying process involved and necessary for
 * generating immutable context objects is just num_of_direct_parents_mapped (P0) +
 * num_of_child_vertices_already_mapped (C) rather than num_of_all_vertices_mapped (C + Pn-1 + Pn-2
 * + ... + P0)
 */
@Value.Immutable
interface ModelVertexProcessingContext extends ProcessingContext {

  Map<ModelPath, ModelVertex> verticesRetrieved();

  default ModelVertexProcessingContext updateContextWithVertex(ModelVertex modelVertex) {
    TreeMap<ModelPath, ModelVertex> modelVertexTreeMap =
        new TreeMap<ModelPath, ModelVertex>(
            Comparator.comparing(ModelPath::depth).thenComparing(ModelPath::uri));
    modelVertexTreeMap.putAll(verticesRetrieved());
    while (!modelVertexTreeMap.isEmpty()
        && modelVertex.vertexPath().depth() - modelVertexTreeMap.firstKey().depth() > 1) {
      modelVertexTreeMap.pollFirstEntry();
    }
    modelVertexTreeMap.put(modelVertex.vertexPath(), modelVertex);
    return ModelVertexProcessingContextImpl.builder()
        .putAllVerticesRetrieved(modelVertexTreeMap)
        .build();
  }
}
