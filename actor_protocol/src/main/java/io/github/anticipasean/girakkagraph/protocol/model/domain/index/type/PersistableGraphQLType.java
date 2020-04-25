package io.github.anticipasean.girakkagraph.protocol.model.domain.index.type;

import akka.japi.Pair;
import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableSet;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.ModelPath;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.ModelPathImpl;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Supplier;
import javax.persistence.metamodel.Attribute;
import org.immutables.criteria.Criteria;
import org.immutables.criteria.repository.async.AsyncReadable;
import org.immutables.criteria.repository.async.AsyncWritable;
import org.immutables.value.Value;

@Criteria
@Criteria.Repository(facets = {AsyncReadable.class, AsyncWritable.class})
@Value.Immutable
@Value.Style(
    depluralize = true,
    depluralizeDictionary = {"attributePath:attributePaths"},
    typeImmutable = "*Impl",
    overshadowImplementation = true)
public interface PersistableGraphQLType extends PersistableType, GraphQLModelType {

  static String deriveSlugNameFromObjectTypeDefinition(GraphQLObjectType graphQLObjectType) {
    return deriveSlugNameFromObjectTypeDefinitionName(graphQLObjectType.getName());
  }

  static String deriveSlugNameFromObjectTypeDefinitionName(String graphQlObjectTypeName) {
    return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, graphQlObjectTypeName);
  }

  @Criteria.Id
  @Value.Derived
  @Override
  default ModelPath path() {
    return ModelPathImpl.builder()
        .addSegment(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, graphQlObjectTypeName()))
        .build();
  }

  @Value.Derived
  default String slugName() {
    return deriveSlugNameFromObjectTypeDefinition(graphQlObjectType());
  }

  @Value.Derived
  default Set<ModelPath> attributePaths() {
    if (jpaManagedType().getAttributes().size()
        != graphQlObjectType().getFieldDefinitions().size()) {
      Supplier<String> messageSupplier =
          () ->
              String.format(
                  "the count of jpa attributes on managed type [ type_name: %s, attribute_count: %d ]"
                      + " does not match the count of graphql field definitions for the "
                      + "corresponding graphql object type [ graphql_object_type: %s, "
                      + "attribute_field_def_count: %d ]",
                  jpaManagedType().getJavaType().getName(),
                  jpaManagedType().getAttributes().size(),
                  graphQlObjectType(),
                  graphQlObjectType().getFieldDefinitions().size());
      throw new IllegalArgumentException(messageSupplier.get());
    }
    Iterator<String> jpaAttrNamesIter =
        jpaManagedType().getAttributes().stream()
            .map(Attribute::getName)
            .map(String::toLowerCase)
            .sorted()
            .iterator();
    Iterator<Pair<String, String>> graphqlAttrNameNormalizedAndOriginalPairIter =
        graphQlObjectType().getFieldDefinitions().stream()
            .map(GraphQLFieldDefinition::getName)
            .map(gqlFieldName -> Pair.create(gqlFieldName.toLowerCase(), gqlFieldName))
            .sorted(Comparator.comparing(Pair::first))
            .iterator();
    ImmutableSet.Builder<ModelPath> attributeModelPathSetBuilder = ImmutableSet.builder();
    while (jpaAttrNamesIter.hasNext() && graphqlAttrNameNormalizedAndOriginalPairIter.hasNext()) {
      String jpaAttrName = jpaAttrNamesIter.next();
      Pair<String, String> graphqlAttrNameNormalizedAndOriginalPair =
          graphqlAttrNameNormalizedAndOriginalPairIter.next();
      if (!jpaAttrName.equalsIgnoreCase(graphqlAttrNameNormalizedAndOriginalPair.first())) {
        Supplier<String> messageSupplier =
            () ->
                String.format(
                    "one of the attributes for this jpa managed type does not match "
                        + "the name of the attribute on the graphql object type: "
                        + "[ jpa_attribute_name: %s, graphql_field_def_name_norm_and_orig_pair: %s ]",
                    jpaAttrName, graphqlAttrNameNormalizedAndOriginalPair.toString());
        throw new IllegalArgumentException(messageSupplier.get());
      }
      attributeModelPathSetBuilder.add(
          ModelPathImpl.builder()
              .from(path())
              .addSegment(graphqlAttrNameNormalizedAndOriginalPair.second())
              .build());
    }
    return attributeModelPathSetBuilder.build();
  }

  //  boolean hasSingularBasicIdAttribute();
  //
  //  Optional<PersistableGraphQLAttribute> singularBasicIdAttributeIfApplicable();

  //  @Value.Check
  //  default void checkPersistableAttributesMatchExpectedNaming() {
  //    Preconditions.checkState(
  //        attributeNameToInstanceEntries().values().stream()
  //            .allMatch(
  //                persistableGraphQLAttribute ->
  //                    persistableGraphQLAttribute.parentPath().equals(typePath())),
  //        "one or more persistable graphql attributes being added to new "
  //            + "persistable graphql type does not have this type as its parent or "
  //            + "has a parent path that does not match that of this one");
  //    Preconditions.checkArgument(
  //        attributeNameToInstanceEntries().entrySet().stream()
  //            .allMatch(entry -> entry.getKey().equals(entry.getValue().name())),
  //        "all persistable graphql attribute name keys must match the attribute name property
  // value");
  //  }
}
/*
  static URI createAttributeUriForNormalizedParentName(
      String normalizedParentName, String graphQLFieldName) {
    return URI.create(
        "model:///"
            + Objects.requireNonNull(normalizedParentName, "normalizedParentName")
            + "/"
            + Objects.requireNonNull(graphQLFieldName, "graphQlFieldName").toLowerCase());
  }

  static URI createAttributeUriForGraphQlOutputTypeParent(
      GraphQLOutputType parentGraphQlOutputType, String graphQLFieldName) {

    return createAttributeUriForNormalizedParentName(
        Objects.requireNonNull(parentGraphQlOutputType, "parentGraphQlOutputType")
            .getName()
            .toLowerCase(),
        graphQLFieldName);
  }

  static URI createTypeUriFromGraphQlOutputType(GraphQLOutputType graphQLOutputType) {
    return URI.create(
        "model:///"
            + Objects.requireNonNull(graphQLOutputType, "graphQLOutputType")
                .getName()
                .toLowerCase());
  }
    static URI stringModelPathToUri(String modelPath) {
    return URI.create("model://" + Objects.requireNonNull(modelPath, "modelPath"));
  }
* */
