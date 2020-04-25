package io.github.anticipasean.girakkagraph.protocol.graphql.state.query;

import io.github.anticipasean.girakkagraph.protocol.graphql.state.GraphQLState;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.ModelLookUpCriteriaHashable;
import graphql.schema.DataFetchingEnvironment;
import org.immutables.value.Value;

public interface GraphQLQueryState extends GraphQLState {

  @Value.Immutable
  interface NoGraphQLContext extends GraphQLQueryState {}

  @Value.Immutable
  interface GraphQLDataFetchingContext extends GraphQLQueryState {
    DataFetchingEnvironment dataFetchingEnvironment();
  }

  @Value.Immutable
  interface HashableModelLookUpCriteria extends GraphQLQueryState {
    DataFetchingEnvironment dataFetchingEnvironment();

    ModelLookUpCriteriaHashable modelLookUpCriteriaHashable();
  }

//  @Value.Immutable
//  interface ModelTypeAndAttributesFound extends GraphQLQueryState {
//    DataFetchingEnvironment dataFetchingEnvironment();
//
//    ModelLookUpCriteriaHashable modelLookUpCriteriaHashable();
//
//    ModelPathMap modelPathMap();
//  }

//  @Value.Immutable
//  interface CriteriaQueryDetermined extends GraphQLQueryState {
//    DataFetchingEnvironment dataFetchingEnvironment();
//
//    ModelLookUpCriteriaHashable modelLookUpCriteriaHashable();
//
//    ModelPathMap modelPathMap();
//
//    CriteriaQuery<?> criteriaQuery();
//  }

//  @Value.Immutable
//  interface DataFetched extends GraphQLQueryState {
//    DataFetchingEnvironment dataFetchingEnvironment();
//
//    ModelLookUpCriteriaHashable modelLookUpCriteriaHashable();
//
//    ModelPathMap modelPathMap();
//
//    CriteriaQuery<?> criteriaQuery();
//
//    DataFetcherResult<?> dataFetcherResult();
//  }
}
