package io.github.anticipasean.girakkagraph.protocol.model.command.modelindex;

import akka.NotUsed;
import io.github.anticipasean.girakkagraph.protocol.model.domain.metamodel.MetaModelDatabase;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
public interface EntityMetaModelIndexed extends ModelIndexService<NotUsed> {
  Optional<MetaModelDatabase> metaModelDatabase();
}
