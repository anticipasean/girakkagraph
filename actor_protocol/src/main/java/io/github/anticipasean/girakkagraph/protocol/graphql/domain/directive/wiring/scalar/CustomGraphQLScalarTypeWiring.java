package io.github.anticipasean.girakkagraph.protocol.graphql.domain.directive.wiring.scalar;

import graphql.schema.GraphQLScalarType;
import java.util.Set;
import java.util.function.Predicate;
import org.slf4j.Logger;

public interface CustomGraphQLScalarTypeWiring extends GraphQLScalarTypeWiring {

  Set<String> customGraphQLScalarTypeNames();

  Logger logger();

  @Override
  default Predicate<GraphQLScalarType> shouldApplyDirectiveToWiringElement() {
    return graphQLScalarType ->
        customGraphQLScalarTypeNames().contains(graphQLScalarType.getName());
  }

  @Override
  default GraphQLScalarType onWiringElementEncountered(GraphQLScalarType wiringElement) {

//    logger()
//        .info(
//            "on_scalar: [ \n\t"
//                + "custom_scalar_type: "
//                + customScalarFieldWiring.startingScalarType()
//                + "\n\tcustom_scalar_type.hash_code: "
//                + customScalarFieldWiring.startingScalarType().hashCode()
//                + "\n]");
    //      if (environment.getElement().hashCode()
    //          != customScalarFieldWiring.startingScalarTypeHashCode()) {
    //        CustomScalarFieldWiring updatedCustomScalarFieldWiring =
    //            ((CustomScalarFieldWiringImpl) customScalarFieldWiring)
    //                .withScalarTypeAfterTypeRegistryUpdate(environment.getElement());
    //        logger()
    //            .info(
    //                "updated_custom_scalar_wiring: from "
    //                    + updatedCustomScalarFieldWiring.updatedScalarTypeHashCode()
    //                    + " back to "
    //                    + customScalarFieldWiring.startingScalarTypeHashCode()
    //                    + ": "
    //                    + updatedCustomScalarFieldWiring);
    //        customScalarTypeNameToFieldWiring()
    //            .put(wiringElement, updatedCustomScalarFieldWiring);
    //        return customScalarFieldWiring.startingScalarType();
    //      }
    return wiringElement;
  }

}
