package io.github.anticipasean.girakkagraph.protocol.graphql.domain.schema.impl;

import akka.japi.Pair;
import com.google.common.collect.ImmutableSet;
import graphql.language.ScalarTypeDefinition;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLTypeReference;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.SchemaPrinter;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.WiringFactory;
import io.github.anticipasean.girakkagraph.protocol.graphql.domain.scalar.GraphQLScalarsSupport;
import io.github.anticipasean.girakkagraph.protocol.graphql.domain.schema.GraphQLFieldDefinitionProcessingContext;
import io.github.anticipasean.girakkagraph.protocol.graphql.domain.schema.GraphQLFieldDefinitionProcessingContextImpl;
import io.github.anticipasean.girakkagraph.protocol.graphql.domain.schema.JpaMetaModelGraphQLSchemaExtractor;
import io.github.anticipasean.girakkagraph.style.naming.EntityNamingConvention;
import io.github.anticipasean.girakkagraph.typematcher.TypeMatcher;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MappedSuperclassType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JpaMetaModelGraphQLSchemaExtractorImpl implements JpaMetaModelGraphQLSchemaExtractor {

  private final WiringFactory wiringFactory;
  private final EntityNamingConvention<?> entityNamingConvention;
  private EntityManager entityManager;
  private Logger logger;

  public JpaMetaModelGraphQLSchemaExtractorImpl(
      EntityManager entityManager,
      WiringFactory wiringFactory,
      EntityNamingConvention<?> entityNamingConvention) {
    this.entityManager = entityManager;
    this.wiringFactory = wiringFactory;
    this.entityNamingConvention = entityNamingConvention;
    this.logger = LoggerFactory.getLogger(JpaMetaModelGraphQLSchemaExtractorImpl.class);
  }

  @Override
  public Set<EntityNamingConvention<?>> entityNamingConventions() {
    return ImmutableSet.of(entityNamingConvention);
  }

  @Override
  public EntityManager entityManager() {
    return this.entityManager;
  }

  @Override
  public GraphQLSchema extractGraphQLSchemaFromJpaMetaModel() {
    logger.info("extracting_graphql_schema_from_jpa_metamodel: []");
    Map<ManagedType<?>, GraphQLObjectType> managedTypeInitialGraphQLObjectTypeMap =
        Stream.of(entityManager.getMetamodel())
            .map(Metamodel::getManagedTypes)
            .flatMap(Collection::parallelStream)
            .map(this::managedTypeToManagedTypeInitialGraphQLObjectTypePair)
            .collect(Collectors.toConcurrentMap(Pair::first, Pair::second));
    logProgressWithManagedTypeInitialGraphQLObjectTypeMap(managedTypeInitialGraphQLObjectTypeMap);
    GraphQLFieldDefinitionProcessingContext graphQLFieldDefinitionProcessingContext =
        GraphQLFieldDefinitionProcessingContextImpl.builder()
            .putAllManagedTypeToInitialGraphQLObjectTypeMap(managedTypeInitialGraphQLObjectTypeMap)
            .build();
    ConcurrentHashMap<GraphQLObjectType, List<GraphQLFieldDefinition>>
        graphQLObjectTypeToFieldDefinitionsMap =
            graphQLFieldDefinitionProcessingContext.managedTypeToInitialGraphQLObjectTypeMap()
                .keySet().stream()
                .parallel()
                .unordered()
                .map(ManagedType::getAttributes)
                .flatMap(Collection::parallelStream)
                .map(pairAttributeWithImmutableContext(graphQLFieldDefinitionProcessingContext))
                .map(this::useAttributeContextPairToCreateGraphQLObjectFieldDefinitionPair)
                .collect(collectGraphQLObjectTypeFieldDefPairsIntoMap());
    List<GraphQLFieldDefinition> topLevelGraphQLFieldDefinitions =
        graphQLObjectTypeToFieldDefinitionsMap.entrySet().stream()
            .parallel()
            .unordered()
            .map(incorporateFieldDefinitionsInParentGraphQLObjectType())
            .map(convertGraphQLObjectTypesToFieldDefinitionsForTopLevelQueryObjectTypeCreation())
            .collect(Collectors.toList());
    GraphQLObjectType queryObjectType =
        createQueryTopLevelGraphQLObjectType(topLevelGraphQLFieldDefinitions);
    logger.info(
        String.format(
            "extracting_graphql_schema_from_jpa_metamodel: [ query_object_type: %s ]",
            queryObjectType.toString()));
    GraphQLSchema initialGraphQLSchema = GraphQLSchema.newSchema().query(queryObjectType).build();
    SchemaPrinter.Options options =
        SchemaPrinter.Options.defaultOptions()
            .includeDirectives(false)
            .includeExtendedScalarTypes(true)
            .includeScalarTypes(true)
            .includeSchemaDefinition(true);
    String initialPrintedSchema = new SchemaPrinter(options).print(initialGraphQLSchema);
    //    debugInitialPrintedSchema(initialPrintedSchema);
    TypeDefinitionRegistry initialTypeDefinitionRegistry =
        new SchemaParser().parse(initialPrintedSchema);
    RuntimeWiring runtimeWiring =
        RuntimeWiring.newRuntimeWiring().wiringFactory(wiringFactory).build();
    return new SchemaGenerator().makeExecutableSchema(initialTypeDefinitionRegistry, runtimeWiring);
  }

  private void debugInitialPrintedSchema(String initialPrintedSchema) {
    try {
      Files.write(
          Paths.get(
              URI.create(
                  "")),
          initialPrintedSchema.getBytes(StandardCharsets.UTF_8),
          StandardOpenOption.TRUNCATE_EXISTING,
          StandardOpenOption.WRITE);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void logProgressWithManagedTypeInitialGraphQLObjectTypeMap(
      Map<ManagedType<?>, GraphQLObjectType> managedTypeInitialGraphQLObjectTypeMap) {
    logger.info(
        String.format(
            "extracting_graphql_schema_from_jpa_metamodel: [ size: %d managed types, example_first_entry: %s ]",
            managedTypeInitialGraphQLObjectTypeMap.size(),
            managedTypeInitialGraphQLObjectTypeMap.size() == 0
                ? ""
                : managedTypeInitialGraphQLObjectTypeMap.entrySet().stream()
                    .findFirst()
                    .map(
                        managedTypeGraphQLObjectTypeEntry ->
                            "[ "
                                + String.join(
                                    ": ",
                                    managedTypeGraphQLObjectTypeEntry
                                        .getKey()
                                        .getJavaType()
                                        .getSimpleName(),
                                    managedTypeGraphQLObjectTypeEntry.getValue().toString())
                                + " ]")));
  }

  private ScalarTypeDefinition generateScalarDefinitionFromScalarTypeCoercedClassTypePairIfAbsent(
      GraphQLScalarType graphQLScalarType) {
    if (graphQLScalarType.getDefinition() != null) {
      return graphQLScalarType.getDefinition();
    }
    return ScalarTypeDefinition.newScalarTypeDefinition().name(graphQLScalarType.getName()).build();
  }

  private Function<Attribute<?, ?>, Pair<Attribute<?, ?>, GraphQLFieldDefinitionProcessingContext>>
      pairAttributeWithImmutableContext(
          GraphQLFieldDefinitionProcessingContext graphQLFieldDefinitionProcessingContext) {
    return attribute -> Pair.create(attribute, graphQLFieldDefinitionProcessingContext);
  }

  private Collector<
          Pair<GraphQLObjectType, GraphQLFieldDefinition>,
          ?,
          ConcurrentHashMap<GraphQLObjectType, List<GraphQLFieldDefinition>>>
      collectGraphQLObjectTypeFieldDefPairsIntoMap() {
    return Collectors.groupingByConcurrent(
        Pair::first,
        ConcurrentHashMap::new,
        Collectors.collectingAndThen(
            Collectors.toList(),
            pairs -> pairs.stream().map(Pair::second).collect(Collectors.toList())));
  }

  private Function<Map.Entry<GraphQLObjectType, List<GraphQLFieldDefinition>>, GraphQLObjectType>
      incorporateFieldDefinitionsInParentGraphQLObjectType() {
    return graphQLObjectTypeListEntry ->
        graphQLObjectTypeListEntry
            .getKey()
            .transform(builder -> builder.fields(graphQLObjectTypeListEntry.getValue()));
  }

  private Function<GraphQLObjectType, GraphQLFieldDefinition>
      convertGraphQLObjectTypesToFieldDefinitionsForTopLevelQueryObjectTypeCreation() {
    return graphQLObjectType ->
        GraphQLFieldDefinition.newFieldDefinition()
            .name(convertTypeNameToFieldName(graphQLObjectType))
            .type(graphQLObjectType)
            .description(queryLevelFieldDescription(graphQLObjectType))
            .build();
  }

  private String convertTypeNameToFieldName(GraphQLObjectType graphQLObjectType) {
    String graphQLObjectTypeName =
        Objects.requireNonNull(graphQLObjectType, "graphQLObjectType").getName();
    if (graphQLObjectTypeName.isEmpty()) {
      Supplier<String> messageSupplier =
          () ->
              String.format(
                  "graphQLObjectType must have a name provided; no name provided for %s",
                  graphQLObjectType);
      throw new IllegalArgumentException(messageSupplier.get());
    }
    return new StringBuilder()
        .append(Character.toLowerCase(graphQLObjectTypeName.charAt(0)))
        .append(graphQLObjectTypeName.substring(1))
        .toString();
  }

  private String queryLevelFieldDescription(GraphQLObjectType graphQLObjectType) {
    return String.format("managed type %s", graphQLObjectType.getName());
  }

  private GraphQLObjectType createQueryTopLevelGraphQLObjectType(
      List<GraphQLFieldDefinition> topLevelGraphQLFieldDefinitions) {
    return GraphQLObjectType.newObject()
        .name("Query")
        .description("Top level query object type containing all schema entity and embedded types")
        .fields(topLevelGraphQLFieldDefinitions)
        .build();
  }

  private Pair<GraphQLObjectType, GraphQLFieldDefinition>
      useAttributeContextPairToCreateGraphQLObjectFieldDefinitionPair(
          Pair<Attribute<?, ?>, GraphQLFieldDefinitionProcessingContext> attributeContextPair) {
    Attribute<?, ?> attribute = attributeContextPair.first();
    GraphQLFieldDefinitionProcessingContext context = attributeContextPair.second();
    if (shouldMapToAScalarType(attribute)) {
      return createGraphQLObjectTypeFieldDefinitionPairForLikelyScalarBasicAttributeUsingContext(
          attribute, context);
    }
    if (shouldMapAnAssociationWithAnotherManagedType(attribute)) {
      return createGraphQLObjectTypeFieldDefinitionPairForAttributeWithManagedTypeAssociationUsingContext(
          attribute, context);
    }
    //    if(shouldMapToAnEmbeddedManagedType(attribute)){
    // TODO: Determine whether embedded types need to be handled differently for attribute graphql

    //    }
    // field definition purposes or if they can remain treated the same as non-basic attributes
    Supplier<String> messageSupplier =
        () ->
            String.format(
                "the attribute [ name: %s, type: %s ] falls "
                    + "into a situation not handled by the graphql schema generation code",
                attribute.getName(), attribute.getJavaType().getSimpleName());
    throw new UnsupportedOperationException(messageSupplier.get());
  }

  private Pair<GraphQLObjectType, GraphQLFieldDefinition>
      createGraphQLObjectTypeFieldDefinitionPairForLikelyScalarBasicAttributeUsingContext(
          Attribute<?, ?> attribute, GraphQLFieldDefinitionProcessingContext context) {
    Optional<GraphQLScalarType> mappedScalarTypeMaybe =
        coercibleJavaTypeGraphQlScalarMapper().apply(attribute::getJavaType);
    if (mappedScalarTypeMaybe.isPresent()) {
      return createGraphQLObjectTypeGraphQLFieldDefinitionPair(
          context, attribute, mappedScalarTypeMaybe::get);
    } else {
      logger.warn(
          String.format(
              "creating_graphql_object_field_def: attribute:[ name: %s, java_type: %s ] expected scalar but got %s",
              attribute.getName(),
              attribute.getJavaType().getSimpleName(),
              attribute.getJavaType().getSimpleName()));
      return createGraphQLObjectTypeGraphQLFieldDefinitionPair(
          context,
          attribute,
          () ->
              GraphQLObjectType.newObject().name(attribute.getJavaType().getSimpleName()).build());
    }
  }

  private boolean scalarTypeExtendedOrCustomIfPresent(
      Optional<GraphQLScalarType> mappedScalarTypeMaybe) {
    return mappedScalarTypeMaybe
        .map(GraphQLScalarType::getName)
        .filter(name -> GraphQLScalarsSupport.extendedScalarsMap().containsKey(name))
        .isPresent();
  }

  private Pair<GraphQLObjectType, GraphQLFieldDefinition>
      createGraphQLObjectTypeFieldDefinitionPairForAttributeWithManagedTypeAssociationUsingContext(
          Attribute<?, ?> attribute, GraphQLFieldDefinitionProcessingContext context) {
    Class<?> jpaEntityClassTypeAssociated = null;
    if (attribute.isCollection() && attribute instanceof PluralAttribute) {
      // bindable java type maps to the element type for collections
      jpaEntityClassTypeAssociated = ((PluralAttribute<?, ?, ?>) attribute).getBindableJavaType();
    } else if (attribute instanceof SingularAttribute) {
      jpaEntityClassTypeAssociated = ((SingularAttribute<?, ?>) attribute).getBindableJavaType();
    }
    // It's possible to have more than one mapping for embeddable types
    Set<ManagedType<?>> managedTypesMappingToJpaEntityClass =
        context.jpaEntityClassToManagedTypeMap().get(jpaEntityClassTypeAssociated);
    ManagedType<?> managedType =
        retrieveManagedTypeMappingToJpaEntityClassForAttributeUsingContext(
            attribute, managedTypesMappingToJpaEntityClass);
    GraphQLObjectType attributeGraphQLObjectType =
        Objects.requireNonNull(
            context.managedTypeToInitialGraphQLObjectTypeMap().get(managedType),
            "managedType mapped to a null value instead of a graphql object type");
    if (attribute.isCollection()) {
      // TODO: still need to implement how sets and maps should be handled in graphql
      // translation
      return createGraphQLObjectTypeGraphQLFieldDefinitionPair(
          context,
          attribute,
          () ->
              GraphQLList.list(GraphQLTypeReference.typeRef(attributeGraphQLObjectType.getName())));
    } else {
      return createGraphQLObjectTypeGraphQLFieldDefinitionPair(
          context,
          attribute,
          () -> GraphQLTypeReference.typeRef(attributeGraphQLObjectType.getName()));
    }
  }

  private ManagedType<?> retrieveManagedTypeMappingToJpaEntityClassForAttributeUsingContext(
      Attribute<?, ?> attribute, Set<ManagedType<?>> managedTypesMappingToJpaEntityClass) {
    return managedTypesMappingToJpaEntityClass.stream()
        .findFirst()
        .orElseThrow(
            () ->
                new NoSuchElementException(
                    String.format(
                        "no managed type was found matching this jpa entity class %s",
                        attribute.getJavaType().getSimpleName())));
  }

  private boolean shouldMapToAScalarType(Attribute<?, ?> attribute) {
    return attribute.getPersistentAttributeType().equals(Attribute.PersistentAttributeType.BASIC);
  }

  private Pair<GraphQLObjectType, GraphQLFieldDefinition>
      createGraphQLObjectTypeGraphQLFieldDefinitionPair(
          GraphQLFieldDefinitionProcessingContext context,
          Attribute attribute,
          Supplier<GraphQLOutputType> graphQLTypeSupplier) {
    return Pair.create(
        context.managedTypeToInitialGraphQLObjectTypeMap().get(attribute.getDeclaringType()),
        GraphQLFieldDefinition.newFieldDefinition()
            .type(graphQLTypeSupplier.get())
            .name(attribute.getName())
            .description(attributeDescription(attribute))
            .build());
  }

  private String attributeDescription(Attribute<?, ?> attribute) {
    return String.format(
        "attribute %s of type %s",
        attribute.getName(),
        attribute.getJavaType().getSimpleName(),
        attribute.getDeclaringType().getJavaType().getSimpleName());
  }

  private boolean shouldMapAnAssociationWithAnotherManagedType(Attribute<?, ?> attribute) {
    return attribute
            .getPersistentAttributeType()
            .equals(Attribute.PersistentAttributeType.ELEMENT_COLLECTION)
        || attribute
            .getPersistentAttributeType()
            .equals(Attribute.PersistentAttributeType.ONE_TO_MANY)
        || attribute
            .getPersistentAttributeType()
            .equals(Attribute.PersistentAttributeType.ONE_TO_ONE)
        || attribute
            .getPersistentAttributeType()
            .equals(Attribute.PersistentAttributeType.MANY_TO_ONE)
        || attribute
            .getPersistentAttributeType()
            .equals(Attribute.PersistentAttributeType.MANY_TO_MANY)
        || attribute
            .getPersistentAttributeType()
            .equals(Attribute.PersistentAttributeType.EMBEDDED);
  }

  private boolean mapsToAnEmbeddedManagedType(Attribute attribute) {
    return attribute
        .getPersistentAttributeType()
        .equals(Attribute.PersistentAttributeType.EMBEDDED);
  }

  private Pair<ManagedType<?>, GraphQLObjectType>
      managedTypeToManagedTypeInitialGraphQLObjectTypePair(ManagedType<?> managedType) {
    return Pair.create(
        managedType,
        GraphQLObjectType.newObject()
            .name(entityInterfaceSimpleName(managedType::getJavaType))
            .description(managedTypeDescription(managedType))
            .build());
  }

  private String managedTypeDescription(ManagedType<?> managedType) {
    String managedTypeInterfaceName = entityInterfaceSimpleName(managedType::getJavaType);
    String descriptionStartBasedOnType =
        TypeMatcher.whenTypeOf(managedType)
            .is(EntityType.class)
            .thenApply(entityType -> "entity: " + managedTypeInterfaceName)
            .is(EmbeddableType.class)
            .thenApply(
                embeddableType ->
                    "embedded type: " + managedTypeInterfaceName + " within other entities")
            .is(MappedSuperclassType.class)
            .thenApply(
                mappedSuperclassType ->
                    "superclass type: " + managedTypeInterfaceName + " extended by other entities")
            .orElseThrow(
                () ->
                    new UnsupportedOperationException(
                        String.format(
                            "a managed type %s within the metamodel cannot be mapped to the current graphql schema configuration",
                            managedType.getJavaType().getSimpleName())));
    return new StringBuilder(descriptionStartBasedOnType)
        .append(databaseName().isPresent() ? " in the " + databaseName().get() + " database" : "")
        .toString();
  }

  @SuppressWarnings("unchecked")
  private <T> String entityInterfaceSimpleName(Supplier<Class<? extends T>> javaTypeSupplier) {
    Supplier<IllegalArgumentException> illegalArgumentExceptionSupplier =
        () ->
            new IllegalArgumentException(
                String.format(
                    "no naming convention supplied applies to the entity associated "
                        + "with class:[ name: %s ]",
                    javaTypeSupplier.get().getSimpleName()));
    EntityNamingConvention<T> applicableEntityNamingConvention =
        (EntityNamingConvention<T>)
            entityNamingConventions().stream()
                .filter(namingConvention -> namingConvention.appliesToClass(javaTypeSupplier.get()))
                .findFirst()
                .orElseThrow(illegalArgumentExceptionSupplier);
    return applicableEntityNamingConvention.entityInterfaceSimpleNameGivenJpaEntityClass(
        javaTypeSupplier.get());
  }

  @Override
  public Optional<String> databaseName() {
    try {
      Connection connection = entityManager.unwrap(Connection.class);
      DatabaseMetaData databaseMetaData = connection.getMetaData();
      return Optional.of(databaseMetaData.getDatabaseProductName());
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  @Override
  public WiringFactory wiringFactory() {
    return wiringFactory;
  }
}
