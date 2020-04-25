package io.github.anticipasean.girakkagraph.protocol.model.command.modelindex;

import io.github.anticipasean.girakkagraph.protocol.model.domain.index.ModelPath;
import java.util.List;
import org.immutables.value.Value;

@Value.Immutable
public interface LookUpModelTypeAndAttributes
    extends ModelIndexService<ModelTypeAndAttributesFound> {
  List<ModelPath> paths();
}
