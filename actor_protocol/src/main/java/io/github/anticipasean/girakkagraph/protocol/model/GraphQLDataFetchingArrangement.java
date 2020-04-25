package io.github.anticipasean.girakkagraph.protocol.model;

import graphql.schema.GraphQLOutputType;
import java.util.Map;
import org.immutables.value.Value;

@Value.Immutable
public interface GraphQLDataFetchingArrangement {
  String rootFieldName();

  GraphQLOutputType rootFieldType();

  Map<String, GraphQLOutputType> selectedQualifiedFieldNamesToGraphQLTypesMap();
}
