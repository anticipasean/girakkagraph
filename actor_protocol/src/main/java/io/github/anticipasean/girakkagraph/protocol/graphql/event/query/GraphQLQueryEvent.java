package io.github.anticipasean.girakkagraph.protocol.graphql.event.query;

import io.github.anticipasean.girakkagraph.protocol.graphql.event.GraphQLEvent;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.ModelLookUpCriteriaHashable;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import javax.persistence.criteria.CriteriaQuery;
import org.immutables.value.Value;

public interface GraphQLQueryEvent extends GraphQLEvent {

  @Value.Immutable
  interface DataFetchingEnvironmentReceived extends GraphQLQueryEvent {
    DataFetchingEnvironment dataFetchingEnvironment();
  }

  @Value.Immutable
  interface HashableModelLookUpCriteriaCalculated extends GraphQLQueryEvent {
    ModelLookUpCriteriaHashable modelLookUpCriteriaHashable();
  }

//  @Value.Immutable
//  interface ModelTypeAndAttributesFound extends GraphQLQueryEvent {
//
//    ModelPathMap modelPathMap();
//  }

  @Value.Immutable
  interface CriteriaQueryDetermined extends GraphQLQueryEvent {

    CriteriaQuery<?> criteriaQuery();
  }

  @Value.Immutable
  interface DataFetched extends GraphQLQueryEvent {

    DataFetcherResult<?> dataFetcherResult();
  }
}
