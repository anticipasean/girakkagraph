package io.github.anticipasean.girakkagraph.protocol.graphql.domain.directive.filter.search;

import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.directives.definition.DirectiveLocations;
import graphql.annotations.annotationTypes.directives.definition.GraphQLDirectiveDefinition;
import graphql.annotations.directives.AnnotationsDirectiveWiring;
import graphql.introspection.Introspection;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@GraphQLName("searchable")
@GraphQLDescription(
    "base filter for annotations indicating arguments should be added to field definitions for searching based on return type")
@GraphQLDirectiveDefinition(
    wiring = AnnotationsDirectiveWiring.class) // dummy directive wiring definition
// actual directive wiring to be done in wiring factory implementation
// annotation and wiring class must be provided on implementing classes
@DirectiveLocations({
  Introspection.DirectiveLocation
      .OBJECT, // May be placed on type (public class ####### {) and thus applied to fields of this
  // type where appropriate
  Introspection.DirectiveLocation
      .FIELD_DEFINITION, // May be placed on the field (public String getName())
  Introspection.DirectiveLocation.INTERFACE // May be placed on interface of type
})
@Retention(
    RetentionPolicy
        .RUNTIME) // Needs to be available at runtime so that the GraphQL Annotations Schema builder
// may see it
/** Meant to be used a template hence abstract: since annotations don't support inheritance */
public @interface Searchable {
  String graphQLName = "searchable";

  @GraphQLName("active")
  @GraphQLDescription("activates this filter for field definitions of the given type")
  boolean active() default true;
}
