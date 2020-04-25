package io.github.anticipasean.girakkagraph.protocol.graphql.domain.schema;

import io.github.anticipasean.girakkagraph.style.naming.EntityNamingConvention;
import io.github.anticipasean.girakkagraph.protocol.graphql.domain.scalar.GraphQLScalarsSupport;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.WiringFactory;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.persistence.EntityManager;

public interface JpaMetaModelGraphQLSchemaExtractor {

  EntityManager entityManager();

  Optional<String> databaseName();

  /**
   * Set of naming conventions for entities to be extracted from the jpa metamodel There should be
   * one naming convention per base type, or else an {@link IllegalArgumentException} will be thrown
   * when called to yield a valid name mapping
   *
   * @return set of naming conventions for the entities involved
   */
  Set<EntityNamingConvention<?>> entityNamingConventions();

  WiringFactory wiringFactory();

  default Function<Supplier<Class<?>>, Optional<GraphQLScalarType>>
      coercibleJavaTypeGraphQlScalarMapper() {
    return GraphQLScalarsSupport.getMapperInstance();
  }

  GraphQLSchema extractGraphQLSchemaFromJpaMetaModel();
}
