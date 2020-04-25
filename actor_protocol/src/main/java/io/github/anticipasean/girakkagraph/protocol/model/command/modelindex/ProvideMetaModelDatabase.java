package io.github.anticipasean.girakkagraph.protocol.model.command.modelindex;

import io.github.anticipasean.girakkagraph.protocol.model.domain.metamodel.MetaModelDatabase;
import org.immutables.value.Value;

@Value.Immutable
public interface ProvideMetaModelDatabase extends ModelIndexService<MetaModelDatabase> {}
