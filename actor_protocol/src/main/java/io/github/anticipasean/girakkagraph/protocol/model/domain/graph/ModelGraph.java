package io.github.anticipasean.girakkagraph.protocol.model.domain.graph;

import akka.japi.Pair;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.ModelPath;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.ModelPathImpl;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.immutables.value.Value;

public interface ModelGraph {

  Set<ModelEdge> edges();

  Map<ModelPath, ModelVertex> vertices();

  @Value.Check
  default void checkVertices() {
    if (!vertices().entrySet().stream()
        .allMatch(entry -> entry.getKey().equals(entry.getValue().vertexPath()))) {
      Supplier<String> messageSupplier =
          () ->
              "all vertex entries must have the model path match the vertex path: "
                  + vertices().entrySet().stream()
                      .filter(entry -> !entry.getKey().equals(entry.getValue().vertexPath()))
                      .map(
                          entry ->
                              String.join(
                                  " != ",
                                  entry.getKey().uri().toString(),
                                  entry.getValue().vertexPath().uri().toString()))
                      .collect(Collectors.joining(", "));
      throw new IllegalArgumentException(messageSupplier.get());
    }
    if (vertices().keySet().stream()
            .filter(
                modelPath ->
                    modelPath.depth() == 1
                        && modelPath.rawArguments().size() == 0
                        && modelPath.directives().size() == 0)
            .count()
        > 1) {
      Supplier<String> messageSupplier =
          () -> "there may not be more than one root vertex within this type of model graph";
      throw new IllegalArgumentException(messageSupplier.get());
    }
  }

  @Value.Lazy
  default Map<Pair<ModelPath, ModelPath>, ModelEdge> edgeMappings() {
    return edges().stream()
        .map(
            modelEdge ->
                Pair.create(Pair.create(modelEdge.parentPath(), modelEdge.childPath()), modelEdge))
        .collect(Collectors.toMap(Pair::first, Pair::second));
  }

  default List<ModelEdge> directedEdgesFromVertex(ModelVertex vertex) {
    return asTree().getOrDefault(vertex, new ArrayList<>());
  }

  default List<ModelEdge> allEdgesForVertex(ModelVertex vertex) {
    Objects.requireNonNull(vertex, "cannot supply null vertex as parameter");
    List<ModelEdge> modelEdgesFrom = asTree().getOrDefault(vertex, new ArrayList<>());
    if (vertex.vertexPath().depth() > 1) {
      List<String> parentPathSegments =
          vertex.vertexPath().segments().subList(0, vertex.vertexPath().segments().size() - 1);
      ModelPath parentVertexPath = ModelPathImpl.of(parentPathSegments);
      Pair<ModelPath, ModelPath> parentChildPathPair =
          Pair.create(parentVertexPath, vertex.vertexPath());
      ModelEdge parentToChildEdge = edgeMappings().get(parentChildPathPair);
      if (parentToChildEdge != null) {
        return Stream.concat(
                Stream.of(parentToChildEdge), Stream.of(modelEdgesFrom).flatMap(Collection::stream))
            .collect(Collectors.toList());
      }
    }
    return modelEdgesFrom;
  }

  default ModelVertex oppositeVertexOnEdge(ModelVertex vertex, ModelEdge edge) {
    Objects.requireNonNull(vertex, "cannot supply null vertex as parameter");
    Objects.requireNonNull(edge, "cannot supply null edge as parameter");
    if (vertices().containsKey(vertex.vertexPath())) {
      if (edge.parentPath().equals(vertex.vertexPath())) {
        return vertices().get(edge.childPath());
      } else if (edge.childPath().equals(vertex.vertexPath())) {
        return vertices().get(edge.parentPath());
      } else {
        String message =
            String.format(
                "vertex [ %s ] does not match either parent or child path on edge [ %s ]",
                vertex.vertexPath(), edge.toString());
        throw new IllegalArgumentException(message);
      }
    }
    String message =
        String.format(
            "vertex [ %s ] not found in vertices map for this graph object", vertex.vertexPath());
    throw new NoSuchElementException(message);
  }

  @Value.Lazy
  default TreeMap<ModelVertex, List<ModelEdge>> asTree() {
    Comparator<ModelVertex> modelPathComparator =
        Comparator.comparing(
            ModelVertex::vertexPath,
            (o1, o2) ->
                o1.depth() - o2.depth() == 0
                    ? o1.uri().compareTo(o2.uri())
                    : o1.depth() - o2.depth());
    TreeMap<ModelVertex, List<ModelEdge>> modelBreadthFirstTree =
        new TreeMap<ModelVertex, List<ModelEdge>>(modelPathComparator);
    Map<ModelPath, List<ModelEdge>> vertexPathToEdges =
        edges().stream().collect(Collectors.groupingBy(ModelEdge::parentPath));
    for (ModelVertex xModelVertex : vertices().values()) {
      modelBreadthFirstTree.put(
          xModelVertex,
          vertexPathToEdges.getOrDefault(xModelVertex.vertexPath(), new ArrayList<>()));
    }
    return modelBreadthFirstTree;
  }

  default Iterator<ModelGraphComponent> traverseBreadthFirst() {
    return asTree().entrySet().stream()
        .flatMap(
            modelVertexListEntry ->
                Stream.concat(
                    Stream.of(modelVertexListEntry.getKey()),
                    modelVertexListEntry.getValue().stream()))
        .iterator();
  }
}
