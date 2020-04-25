package io.github.anticipasean.girakkagraph.protocol.model.domain.graph;

import akka.japi.Pair;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.ModelPath;
import java.util.Comparator;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Parameter;

public interface ModelEdge extends ModelGraphComponent, Comparable<ModelEdge> {

  static Comparator<ModelEdge> comparator() {
    return Comparator.comparing(ModelEdge::edgeKey, EdgeKey.comparator());
  }

  @Derived
  default EdgeKey edgeKey() {
    return EdgeKeyImpl.builder().parentPath(parentPath()).childPath(childPath()).build();
  }

  ModelPath parentPath();

  ModelPath childPath();

  @Value.Check
  default void checkParentPathIsParentToChildPath() {
    if (!(parentPath().isChild(childPath()))) {
      throw new IllegalArgumentException(
          String.format(
              "parent_path [ %s ] is not actually a parent to child_path [ %s ]",
              parentPath().uri(), childPath().uri()));
    }
  }

  @Override
  default int compareTo(ModelEdge modelEdge) {
    return comparator().compare(this, modelEdge);
  }

  @Immutable
  abstract static class EdgeKey implements Comparable<EdgeKey> {

    public static Comparator<EdgeKey> comparator() {
      return Comparator.comparing(EdgeKey::parentPath, ModelPath::compareTo)
          .thenComparing(EdgeKey::childPath, ModelPath::compareTo);
    }

    @Derived
    Pair<ModelPath, ModelPath> parentChildModelPathPair() {
      return Pair.create(parentPath(), childPath());
    }

    @Parameter
    public abstract ModelPath parentPath();

    @Parameter
    public abstract ModelPath childPath();

    @Override
    public String toString() {
      return String.join(" --> ", parentPath().uri().toString(), childPath().uri().toString());
    }

    @Override
    public int compareTo(EdgeKey edgeKey) {
      return comparator().compare(this, edgeKey);
    }
  }
}
