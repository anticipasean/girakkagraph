package io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge;

import com.google.common.base.Preconditions;
import io.github.anticipasean.girakkagraph.protocol.model.domain.argument.ModelArgument;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.ModelEdge;
import org.immutables.value.Value;

public interface ArgumentEdge extends ModelEdge {

  @Value.Check
  default void checkChildContainsArgument() {
    Preconditions.checkArgument(
        parentPath().rawArguments().size() == 0,
        "parent path to an argument edge must not contain any arguments; the child has the argument");
    Preconditions.checkArgument(
        childPath().rawArguments().size() >= 1,
        "child path must have at least one argument for an argument edge to be formed");
  }

  ModelArgument<?, ?> modelArgument();
}
