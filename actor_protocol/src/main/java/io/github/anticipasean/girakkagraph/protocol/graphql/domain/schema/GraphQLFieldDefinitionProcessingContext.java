package io.github.anticipasean.girakkagraph.protocol.graphql.domain.schema;

import akka.japi.Pair;
import io.github.anticipasean.girakkagraph.protocol.model.domain.context.ProcessingContext;
import graphql.schema.GraphQLObjectType;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.persistence.metamodel.ManagedType;
import org.immutables.value.Value;

@Value.Immutable
public interface GraphQLFieldDefinitionProcessingContext extends ProcessingContext {
  Map<ManagedType<?>, GraphQLObjectType> managedTypeToInitialGraphQLObjectTypeMap();

  @Value.Derived
  default Map<Class<?>, Set<ManagedType<?>>> jpaEntityClassToManagedTypeMap() {
    return managedTypeToInitialGraphQLObjectTypeMap()
        .keySet()
        .parallelStream()
        .unordered()
        .map(managedType -> Pair.create(managedType.getJavaType(), managedType))
        .collect(
            Collectors.groupingByConcurrent(
                Pair::first,
                ConcurrentHashMap::new,
                Collectors.collectingAndThen(
                    Collectors.toSet(),
                    pairs -> pairs.stream().map(Pair::second).collect(Collectors.toSet()))));
  }
}
