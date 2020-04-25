package io.github.anticipasean.girakkagraph.protocol.model.domain.metamodel;

import akka.NotUsed;
import akka.japi.Pair;
import akka.stream.ClosedShape;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.GraphDSL;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.attribute.PersistableGraphQLAttribute;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.attribute.PersistableGraphQLAttributeCriteria;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.attribute.PersistableGraphQLAttributeImpl;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.type.PersistableGraphQLType;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.type.PersistableGraphQLTypeCriteria;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.type.PersistableGraphQLTypeImpl;
import io.github.anticipasean.girakkagraph.style.naming.EntityNamingConvention;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import org.immutables.criteria.backend.WriteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetaModelDatabaseCreationGraphGenerator {
  private final Logger logger;
  private final EntityNamingConvention<?> entityNamingConvention;

  public MetaModelDatabaseCreationGraphGenerator(EntityNamingConvention<?> entityNamingConvention) {
    this.logger = LoggerFactory.getLogger(MetaModelDatabaseCreationGraphGenerator.class);
    this.entityNamingConvention = entityNamingConvention;
  }

  public RunnableGraph<CompletionStage<MetaModelDatabase>>
      buildDatabaseCreationGraphUsingJpaMetamodelAndGraphQLSchema(
          Metamodel metamodel, GraphQLSchema graphQLSchema) {
    Source<Pair<PersistableGraphQLType, List<PersistableGraphQLAttribute>>, NotUsed>
        persistableGraphQLTypeSource =
            persistableGraphQLTypeAttributesPairsSource(metamodel, graphQLSchema);
    Sink<
            Pair<PersistableGraphQLType, List<PersistableGraphQLAttribute>>,
            CompletionStage<MetaModelDatabase>>
        metaModelDatabaseCompletionStageSink =
            Sink
                .<MetaModelDatabase,
                    Pair<PersistableGraphQLType, List<PersistableGraphQLAttribute>>>
                    fold(
                        new MetaModelDatabase(),
                        (metaModelDatabase, pair) -> {
                          insertPersistableTypesIntoMetaModelDatabase(
                              pair.first(), metaModelDatabase);
                          insertPersistableTypeAttributesIntoMetaModelDatabase(
                              pair.second(), metaModelDatabase);
                          return metaModelDatabase;
                        })
                .mapMaterializedValue(
                    this::addPersistableGraphQLTypeMappingsToNonBasicAttributesInMetaModelDatabase);

    RunnableGraph<CompletionStage<MetaModelDatabase>> runnableGraph =
        RunnableGraph.fromGraph(
            GraphDSL.create(
                metaModelDatabaseCompletionStageSink,
                (builder, sink) -> {
                  builder.from(builder.add(persistableGraphQLTypeSource)).to(sink);
                  return ClosedShape.getInstance();
                }));
    return runnableGraph;
  }

  private Source<Pair<PersistableGraphQLType, List<PersistableGraphQLAttribute>>, NotUsed>
      persistableGraphQLTypeAttributesPairsSource(
          Metamodel metamodel, GraphQLSchema graphQLSchema) {
    // Converting to lowercase for normalization and comparison
    Map<String, GraphQLObjectType> nameToRootLevelFieldDefMap =
        graphQLSchema.getAllTypesAsList().stream()
            .filter(graphQLType -> graphQLType instanceof GraphQLObjectType)
            .map(graphQLType -> ((GraphQLObjectType) graphQLType))
            .collect(
                Collectors.toMap(
                    graphQLObjectType -> graphQLObjectType.getName().toLowerCase(),
                    graphQLObjectType -> graphQLObjectType));
    return Source.from(metamodel.getManagedTypes())
        .via(
            mapJpaManagedTypesToGraphQLObjectTypeIfMatchingNamingConvention(
                nameToRootLevelFieldDefMap))
        .via(
            mapJpaManagedTypeGraphQLFieldDefPairsToPersistableGraphQLModelTypeAttributeListPairsFlow())
        .async();
  }

  private Flow<ManagedType<?>, Pair<ManagedType<?>, GraphQLObjectType>, NotUsed>
      mapJpaManagedTypesToGraphQLObjectTypeIfMatchingNamingConvention(
          Map<String, GraphQLObjectType> nameToRootLevelObjectTypeMap) {
    return Flow.<ManagedType<?>>create()
        .map(
            managedType -> {
              if (managedTypeFollowsConvention(entityNamingConvention, managedType)) {
                String simpleManagedTypeName = managedType.getJavaType().getSimpleName();
                String normalizedBaseTypeName =
                    simpleManagedTypeName
                        .substring(0, simpleManagedTypeName.length() - 3)
                        .toLowerCase();
                if (nameToRootLevelObjectTypeMap.containsKey(normalizedBaseTypeName)) {
                  GraphQLObjectType graphQLObjectType =
                      nameToRootLevelObjectTypeMap.get(normalizedBaseTypeName);
                  return Pair.create(managedType, graphQLObjectType);
                }
                Supplier<String> messageSupplier =
                    () ->
                        "normalized managed type java type name "
                            + normalizedBaseTypeName
                            + " follows naming rule but"
                            + " does not match a graphql object type name: [ "
                            + nameToRootLevelObjectTypeMap.keySet().stream()
                                .sorted()
                                .collect(Collectors.joining(", "))
                            + " ]";
                throw new IllegalStateException(messageSupplier.get());
              }
              Supplier<String> messageSupplier =
                  () ->
                      "managed type java type name does not follow naming rule; "
                          + "no matching graphql field definition was found for managed type for: "
                          + managedType.getJavaType().getName();
              throw new IllegalStateException(messageSupplier.get());
            });
  }

  @SuppressWarnings("unchecked")
  private <X, T> boolean managedTypeFollowsConvention(
      EntityNamingConvention<X> entityNamingConvention, ManagedType<T> managedType) {
    if (entityNamingConvention.appliesToClass(managedType.getJavaType())) {
      return entityNamingConvention.jpaEntityClassFollowsNamingRules(
          (Class<? extends X>) managedType.getJavaType());
    }
    return false;
  }

  private Flow<
          Pair<ManagedType<?>, GraphQLObjectType>,
          Pair<PersistableGraphQLType, List<PersistableGraphQLAttribute>>,
          NotUsed>
      mapJpaManagedTypeGraphQLFieldDefPairsToPersistableGraphQLModelTypeAttributeListPairsFlow() {
    return Flow.<Pair<ManagedType<?>, GraphQLObjectType>>create()
        .map(
            pair -> {
              //              logger.info(
              //                  String.format(
              //                      "creating index for pair: [ managed_type_class_name: %s,
              // graphql_object_type: %s ]",
              //                      pair.first().getJavaType().getName(), pair.second()));
              Map<String, ? extends Attribute<?, ?>> nameToAttrMap =
                  pair.first().getAttributes().stream()
                      .map(attribute -> Pair.create(attribute.getName().toLowerCase(), attribute))
                      .collect(Collectors.toMap(Pair::first, Pair::second));
              PersistableGraphQLType persistableGraphQLType =
                  PersistableGraphQLTypeImpl.builder()
                      .jpaManagedType(pair.first())
                      .graphQlObjectType(pair.second())
                      .build();
              List<PersistableGraphQLAttribute> persistableGraphQLAttributes =
                  pair.second().getFieldDefinitions().stream()
                      .map(
                          fieldDefinition -> {
                            if (nameToAttrMap.containsKey(
                                fieldDefinition.getName().toLowerCase())) {
                              return PersistableGraphQLAttributeImpl.builder()
                                  .graphQlFieldDefinition(fieldDefinition)
                                  .jpaAttribute(
                                      nameToAttrMap.get(fieldDefinition.getName().toLowerCase()))
                                  .parentType(persistableGraphQLType)
                                  .build();
                            } else {
                              Supplier<String> messageSupplier =
                                  () ->
                                      String.format(
                                          "one of the attribute graphql field definitions [ %s ] does not map to"
                                              + " an attribute name in the attribute set under the metamodel for [ %s ]: \n [ %s ]",
                                          fieldDefinition,
                                          pair.first().getJavaType().getName(),
                                          nameToAttrMap.keySet().stream()
                                              .sorted()
                                              .collect(Collectors.joining(",\n")));
                              throw new IllegalStateException(messageSupplier.get());
                            }
                          })
                      .collect(Collectors.toList());
              return Pair.create(persistableGraphQLType, persistableGraphQLAttributes);
            });
  }

  private MetaModelDatabase insertPersistableTypesIntoMetaModelDatabase(
      PersistableGraphQLType persistableGraphQLType, MetaModelDatabase metaModelDatabase) {
    if (persistableGraphQLType.isEmbeddable()) {
      CompletionStage<Boolean> alreadyInserted =
          metaModelDatabase
              .getTypeRepository()
              .find(
                  PersistableGraphQLTypeCriteria.persistableGraphQLType.path.is(
                      persistableGraphQLType.path()))
              .exists();
      if (alreadyInserted.toCompletableFuture().join()) {
        return metaModelDatabase;
      }
    }
    CompletionStage<WriteResult> writeResultCompletionStage =
        metaModelDatabase.getTypeRepository().insert(persistableGraphQLType);
    WriteResult writeResult = writeResultCompletionStage.toCompletableFuture().join();
    if (!writeResult.insertedCount().isPresent() || writeResult.insertedCount().getAsLong() != 1L) {
      throw new IllegalStateException(
          String.format(
              "the insertion count to the metamodel database for persistableGraphQLType [ %s ] is 0",
              persistableGraphQLType));
    }
    return metaModelDatabase;
  }

  private MetaModelDatabase insertPersistableTypeAttributesIntoMetaModelDatabase(
      List<PersistableGraphQLAttribute> persistableGraphQLAttributes,
      MetaModelDatabase metaModelDatabase) {

    CompletionStage<WriteResult> writeResultCompletionStage =
        metaModelDatabase.getAttributeRepository().upsertAll(persistableGraphQLAttributes);
    WriteResult writeResult = null;
    try {
      writeResult = writeResultCompletionStage.toCompletableFuture().join();
    } catch (Exception e) {
      logger.error("an error occurred when upserting the attributes to the metamodel database", e);
      throw e;
    }
    if (!writeResult.updatedCount().isPresent()
        || writeResult.updatedCount().getAsLong() < persistableGraphQLAttributes.size()) {
      WriteResult finalWriteResult = writeResult;
      Supplier<String> messageSupplier =
          () ->
              String.format(
                  "the insertion count [ %d ] to the metamodel database does not match the expected number of "
                      + "attributes to be inserted into the metamodel database [ %d ] for persistable type [ %s ]",
                  finalWriteResult.updatedCount().orElse(0L),
                  persistableGraphQLAttributes.size(),
                  persistableGraphQLAttributes.size() > 0
                      ? persistableGraphQLAttributes.get(0).parentPath().uri()
                      : "null");
      throw new IllegalStateException(messageSupplier.get());
    }

    return metaModelDatabase;
  }

  private CompletionStage<MetaModelDatabase>
      addPersistableGraphQLTypeMappingsToNonBasicAttributesInMetaModelDatabase(
          CompletionStage<MetaModelDatabase> metaModelDatabaseCompletionStage) {
    return metaModelDatabaseCompletionStage.thenApplyAsync(
        metaModelDatabase -> {
          //          logger.info("beginning to add parent types to non-basic attributes");
          List<PersistableGraphQLAttribute> attributesRequiringUpdate =
              retrieveAttributesRequiringUpdate(metaModelDatabase);
          //          logger.info(
          //              "attributes to update: \n\t"
          //                  + attributesRequiringUpdate.stream()
          //                      .map(PersistableGraphQLAttribute::attributePath)
          //                      .sorted(Comparator.comparing(ModelPath::uri))
          //                      .map(ModelPath::uri)
          //                      .map(URI::toString)
          //                      .collect(Collectors.joining(",\n\t")));
          WriteResult writeResult = null;
          try {
            writeResult =
                metaModelDatabase
                    .getAttributeRepository()
                    .updateAll(attributesRequiringUpdate)
                    .toCompletableFuture()
                    .join();
          } catch (Exception e) {
            logger.error(
                "an error occurred when attempting to update the attributes requiring updates to include their parent type information",
                e);
            throw e;
          }
          if (!writeResult.updatedCount().isPresent()
              || writeResult.updatedCount().getAsLong() < attributesRequiringUpdate.size()) {
            WriteResult finalWriteResult = writeResult;
            Supplier<String> messageSupplier =
                () ->
                    String.format(
                        "the update count [ %d ] to the metamodel database does not match the expected number of "
                            + "attributes to be updated into the metamodel database [ %d ]",
                        finalWriteResult.updatedCount().orElse(0L),
                        attributesRequiringUpdate.size());
            throw new IllegalStateException(messageSupplier.get());
          }
          //          logger.info(
          //              String.format(
          //                  "detected %d attributes requiring non-basic types to be set: updated
          // %d attributes",
          //                  attributesRequiringUpdate.size(),
          // writeResult.updatedCount().orElse(0L)));
          return metaModelDatabase;
        });
  }

  private List<PersistableGraphQLAttribute> retrieveAttributesRequiringUpdate(
      MetaModelDatabase metaModelDatabase) {
    try {
      return getNonBasicAttributesWithoutExistingTypeMappingsInAttributeRepository(
              metaModelDatabase)
          .stream()
          .collect(
              Collectors.groupingBy(
                  PersistableGraphQLAttribute::singularTypeOrPluralAttributeElementType))
          .entrySet()
          .stream()
          .map(
              entry -> processActualJavaTypeToNonBasicAttributesListEntry(metaModelDatabase, entry))
          .flatMap(Collection::stream)
          .collect(Collectors.toList());
    } catch (Exception e) {
      logger.error(
          "an error occurred when obtaining the attributes to update with parent type values", e);
      throw e;
    }
  }

  private List<PersistableGraphQLAttribute>
      getNonBasicAttributesWithoutExistingTypeMappingsInAttributeRepository(
          MetaModelDatabase metaModelDatabase) {
    //    logger.info("retrieving non-basic attributes for update");
    try {
      return metaModelDatabase
          .getAttributeRepository()
          .find(
              PersistableGraphQLAttributeCriteria.persistableGraphQLAttribute
                  .isBasic
                  .isFalse()
                  .modelTypeIfAttributeNotBasic
                  .isAbsent())
          .fetch()
          .toCompletableFuture()
          .join();
    } catch (Exception e) {
      logger.error("error occurred when retrieving the non-basic attributes requiring update", e);
      throw e;
    }
  }

  private List<PersistableGraphQLAttribute> processActualJavaTypeToNonBasicAttributesListEntry(
      MetaModelDatabase metaModelDatabase,
      Map.Entry<? extends Class<?>, List<PersistableGraphQLAttribute>> entry) {
    //    logger.info(
    //        String.format(
    //            "entry: looking for type [ attribute_sing_plur_elem_type: %s, num_of_attributes:
    // %s ]",
    //            entry.getKey().getName(), entry.getValue().size()));
    Optional<PersistableGraphQLType> persistableGraphQLTypeIfFound =
        getNonBasicTypeForNonBasicAttribute(metaModelDatabase, entry);
    if (persistableGraphQLTypeIfFound.isPresent()) {
      return updateAttributesWithMatchingNonBasicType(entry, persistableGraphQLTypeIfFound.get());
    }
    return new ArrayList<PersistableGraphQLAttribute>();
  }

  private Optional<PersistableGraphQLType> getNonBasicTypeForNonBasicAttribute(
      MetaModelDatabase metaModelDatabase,
      Map.Entry<? extends Class<?>, List<PersistableGraphQLAttribute>> entry) {
    try {
      return metaModelDatabase
          .getTypeRepository()
          .find(PersistableGraphQLTypeCriteria.persistableGraphQLType.javaType.is(entry.getKey()))
          .oneOrNone()
          .toCompletableFuture()
          .join();
    } catch (Exception e) {
      logger.error(
          String.format(
              "an error occurred when retrieving the type for these non-basic entries: [ type_name: %s, attribute_count: %s ]",
              entry.getKey().getName(), entry.getValue().size()),
          e);
      throw e;
    }
  }

  private List<PersistableGraphQLAttribute> updateAttributesWithMatchingNonBasicType(
      Map.Entry<? extends Class<?>, List<PersistableGraphQLAttribute>> entry,
      PersistableGraphQLType persistableGraphQLType) {
    return entry.getValue().stream()
        .map(
            persistableGraphQLAttribute ->
                PersistableGraphQLAttributeImpl.builder()
                    .from(persistableGraphQLAttribute)
                    .modelTypeIfAttributeNotBasic(persistableGraphQLType)
                    .build())
        .collect(Collectors.toList());
  }
}
