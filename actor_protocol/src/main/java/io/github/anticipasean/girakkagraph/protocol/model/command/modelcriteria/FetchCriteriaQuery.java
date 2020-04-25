package io.github.anticipasean.girakkagraph.protocol.model.command.modelcriteria;

import io.github.anticipasean.girakkagraph.protocol.model.domain.index.ModelLookUpCriteriaHashable;
import org.immutables.value.Value;

@Value.Immutable
public interface FetchCriteriaQuery extends ModelCriteriaService<CriteriaQueryFetched> {

  ModelLookUpCriteriaHashable modelLookUpCriteriaHashable();

//  ModelPathMap modelPathMap();
}
