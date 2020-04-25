package io.github.anticipasean.girakkagraph.protocol.graphql.domain.directive.wiring.field;

import io.github.anticipasean.girakkagraph.protocol.graphql.domain.directive.filter.search.Searchable;
import io.github.anticipasean.girakkagraph.protocol.graphql.domain.operator.GraphQLArgumentOperator;
import io.github.anticipasean.girakkagraph.protocol.model.domain.arity.Arity;
import graphql.Scalars;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLArgument.Builder;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.immutables.value.Value;


public interface SearchableFieldDirectiveWiring extends GraphQLFieldDefinitionWiring {

  default GraphQLArgument argumentForSearchDirective() {
    return GraphQLArgument.newArgument()
        .name("active")
        .type(Scalars.GraphQLBoolean)
        .defaultValue(Boolean.TRUE)
        .description("adds search filter arguments to fields e.g. contains for string")
        .build();
  }

  default GraphQLDirective searchFilterDirective() {
    return GraphQLDirective.newDirective()
        .name(Searchable.graphQLName)
        .argument(argumentForSearchDirective())
        .build();
  }

  @Override
  default GraphQLFieldDefinition onField(
      SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment) {
    GraphQLFieldDefinition fieldDefinition = environment.getElement();
    if (shouldApplyDirectiveToWiringElement().test(fieldDefinition)) {
      return onWiringElementEncountered(fieldDefinition);
    }
    return fieldDefinition;
  }

  default Class<? extends Annotation> getSearchableDirectiveAnnotationClass() {
    return Searchable.class;
  }

  default String getSearchableDirectiveGraphQLName() {
    return Searchable.graphQLName;
  }

  String graphQLTypeName();

  default GraphQLDirective getDirective() {
    return searchFilterDirective();
  }

  Set<Class<?>> coercibleJavaTypes();

  Set<GraphQLArgumentOperator> compatibleGraphQLArgumentOperators();

  @Value.Derived
  default Map<String, GraphQLArgumentOperator> compatibleGraphQLArgumentNameToGraphQLArgumentOperator() {
    return compatibleGraphQLArgumentOperators().stream()
        .collect(Collectors.toMap(GraphQLArgumentOperator::argumentName, Function.identity()));
  }

  @Value.Derived
  default BiFunction<GraphQLInputType, GraphQLArgumentOperator, GraphQLArgument> graphQLArgumentCreator() {
    return (graphQLInputType, argumentOperator) -> {
      Builder argumentBuilder = GraphQLArgument.newArgument().name(argumentOperator.argumentName());
      if (argumentOperator.description().length() > 0) {
        argumentBuilder.description(argumentOperator.description());
      }
      if (argumentOperator.arity().equals(Arity.TERNARY)) {
        if (!(graphQLInputType instanceof GraphQLList)) {
          GraphQLList graphQLList = GraphQLList.list(graphQLInputType);
          argumentBuilder.type(graphQLList);
        }
      } else {
        argumentBuilder.type(graphQLInputType);
      }
      GraphQLArgument graphQLArgument = argumentBuilder.build();
      return argumentOperator.onApplyOperatorSpecificChangesToGraphQLArgument().apply(graphQLArgument);
    };
  }
}
