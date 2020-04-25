package io.github.anticipasean.girakkagraph.protocol.model.domain.index.indexer;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.type.PersistableGraphQLType;
import graphql.schema.GraphQLSchema;
import java.util.function.BiFunction;
import javax.persistence.metamodel.Metamodel;

public interface PersistableGraphQLTypeMapper
    extends BiFunction<Metamodel, GraphQLSchema, Source<PersistableGraphQLType, NotUsed>> {}
