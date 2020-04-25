package io.github.anticipasean.girakkagraph.protocol.graphql.command.schema.mapping;

import graphql.language.Field;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLOutputType;
import org.immutables.value.Value;

@Value.Immutable
public interface MapSelectionSetToModel
    extends GraphQlSchemaMappingProtocol<SelectionSetMappedToModel> {
  Field rootField();

  GraphQLOutputType rootFieldType();

  DataFetchingFieldSelectionSet dataFetchingFieldSelectionSet();
}
