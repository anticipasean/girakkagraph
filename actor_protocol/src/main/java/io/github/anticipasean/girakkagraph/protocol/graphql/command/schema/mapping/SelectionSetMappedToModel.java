package io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.mapping;

import akka.NotUsed;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.ModelLookUpCriteriaHashable;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
public interface SelectionSetMappedToModel extends GraphQlSchemaMappingProtocol<NotUsed> {
  Optional<ModelLookUpCriteriaHashable> modelLookUpCriteriaHashable();
}
