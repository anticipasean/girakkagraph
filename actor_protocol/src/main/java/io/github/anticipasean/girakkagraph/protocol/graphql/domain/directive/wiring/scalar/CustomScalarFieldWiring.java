package io.github.anticipasean.girakkagraph.protocol.graphql.domain.directive.wiring.scalar;

import io.github.anticipasean.girakkagraph.protocol.graphql.domain.directive.wiring.FocusedDirectiveWiring;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLScalarType;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Immutable
public interface CustomScalarFieldWiring
    extends FocusedDirectiveWiring<GraphQLFieldDefinition> {

  default Logger logger() {
    return LoggerFactory.getLogger(CustomScalarFieldWiring.class);
  }

  GraphQLScalarType startingScalarType();

  @Value.Derived
  default int startingScalarTypeHashCode() {
    return startingScalarType().hashCode();
  }

  Optional<GraphQLScalarType> scalarTypeAfterTypeRegistryUpdate();

  @Value.Derived
  default int updatedScalarTypeHashCode() {
    return scalarTypeAfterTypeRegistryUpdate().map(Objects::hashCode).orElse(-1);
  }

  /**
   * This is called when a field is encountered, which gives the schema directive a chance to modify
   * the shape and behaviour of that DSL element
   *
   * <p>The {@link #onArgument(SchemaDirectiveWiringEnvironment)} callbacks will have been invoked
   * for this element beforehand
   *
   * @param environment the wiring element
   * @return a non null element based on the original one
   */
  @Override
  default GraphQLFieldDefinition onField(
      SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment) {
    if (shouldApplyDirectiveToWiringElement().test(environment.getElement())) {}

    return environment.getElement();
  }

  @Override
  default Predicate<GraphQLFieldDefinition> shouldApplyDirectiveToWiringElement() {
    return graphQLFieldDefinition ->
        graphQLFieldDefinition.getType() instanceof GraphQLScalarType
            && ((GraphQLScalarType) graphQLFieldDefinition.getType())
                .getName()
                .equals(startingScalarType().getName());
  }

  @Override
  default GraphQLFieldDefinition onWiringElementEncountered(GraphQLFieldDefinition wiringElement) {
    logger().info("on_field: before_update: " + wiringElement);
    logger()
        .info(
            String.format(
                "scalar_type.name: %s, scalar_type.hash_code: %d",
                ((GraphQLScalarType) wiringElement.getType()).getName(),
                ((GraphQLScalarType) wiringElement.getType()).hashCode()));
    GraphQLScalarType currentGraphQLScalarType = ((GraphQLScalarType) wiringElement.getType());
    if (currentGraphQLScalarType.hashCode() != startingScalarTypeHashCode()
        && scalarTypeAfterTypeRegistryUpdate().isPresent()) {
      GraphQLFieldDefinition updatedDefinition =
          wiringElement.transform(builder -> builder.type(startingScalarType()));
      logger().info("updated_field_def: " + updatedDefinition);
      logger()
          .info(
              String.format(
                  "scalar_type.name: %s, scalar_type.hash_code: %d",
                  ((GraphQLScalarType) updatedDefinition.getType()).getName(),
                  updatedDefinition.getType().hashCode()));
      return updatedDefinition;
    }
    return wiringElement;
  }
}
