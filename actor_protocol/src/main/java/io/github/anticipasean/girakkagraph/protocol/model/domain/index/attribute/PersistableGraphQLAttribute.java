package io.github.anticipasean.girakkagraph.protocol.model.domain.index.attribute;

import com.google.common.base.CaseFormat;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.ModelPath;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.ModelPathImpl;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.type.PersistableGraphQLType;
import java.util.Optional;
import org.immutables.criteria.Criteria;
import org.immutables.criteria.repository.async.AsyncReadable;
import org.immutables.criteria.repository.async.AsyncWritable;
import org.immutables.value.Value;

@Criteria
@Criteria.Repository(facets = {AsyncReadable.class, AsyncWritable.class})
@Value.Immutable
public interface PersistableGraphQLAttribute extends PersistableAttribute, GraphQLModelAttribute {

  @Value.Derived
  @Criteria.Id
  @Override
  default ModelPath path() {
    return ModelPathImpl.builder().from(parentPath()).addSegment(graphQlFieldName()).build();
  }

  @Value.Derived
  default String slugName() {
    return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, graphQlFieldName());
  }

  @Value.Derived
  default ModelPath parentPath() {
    return ModelPathImpl.builder()
        .addSegment(
            CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, parentGraphQlObjectTypeName()))
        .build();
  }

  PersistableGraphQLType parentType();

  @Value.Derived
  default String parentGraphQlObjectTypeName() {
    return parentType().graphQlObjectTypeName();
  }

  Optional<PersistableGraphQLType> modelTypeIfAttributeNotBasic();

  @Value.Derived
  default void operatorsAvailable() {}
}

/*
@Value.Default
  @Criteria.Id
  default URI uri() {
    return PersistableGraphQLType.createAttributeUriForNormalizedParentName(
        Optional.of(parentUri())
            .filter(uri -> uri.getPath().indexOf('/') == 0)
            .map(uri -> uri.getPath().substring(1))
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "parent uri is not in the expected format: " + parentUri())),
        fieldName());
  }
* */
