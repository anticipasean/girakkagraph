package io.github.anticipasean.girakkagraph.protocol.graphql.domain.directive.wiring.scalar;

import akka.japi.Pair;
import io.github.anticipasean.girakkagraph.protocol.graphql.domain.directive.wiring.FocusedDirectiveWiringProvider;
import io.github.anticipasean.girakkagraph.protocol.graphql.domain.scalar.GraphQLScalarsSupport;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLScalarType;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.immutables.value.Value.Immutable;

/**
 * Context:
 *
 * <p>graphql.schema.idl.SchemaGenerator#buildScalar(graphql.schema.idl.SchemaGenerator.BuildContext,
 * graphql.language.ScalarTypeDefinition)
 *
 * <pre>
 *           if (!ScalarInfo.isStandardScalar(scalar) && !ScalarInfo.isGraphqlSpecifiedScalar(scalar)) {
 *             scalar = scalar.transform(builder -> builder
 *                     .definition(typeDefinition)
 *                     .comparatorRegistry(buildCtx.getComparatorRegistry())
 *                     .withDirectives(
 *                             buildDirectives(typeDefinition.getDirectives(),
 *                                     directivesOf(extensions), SCALAR, buildCtx.getDirectiveDefinitions(), buildCtx.getComparatorRegistry())
 *                     ));
 *             //
 *             // only allow modification of custom scalars
 *             scalar = directiveBehaviour.onScalar(scalar, buildCtx.mkBehaviourParams());
 *         }
 *         return scalar;
 * </pre>
 *
 * graphql.schema.GraphQLTypeCollectingVisitor#assertTypeUniqueness(graphql.schema.GraphQLNamedType,
 * java.util.Map)
 *
 * <pre>
 *       private void assertTypeUniqueness(GraphQLNamedType type, Map<String, GraphQLNamedType> result) {
 *         GraphQLType existingType = result.get(type.getName());
 *         // do we have an existing definition
 *         if (existingType != null) {
 *             // type references are ok
 *             if (!(existingType instanceof GraphQLTypeReference || type instanceof GraphQLTypeReference))
 *                 // object comparison here is deliberate
 *                 if (existingType != type) {
 *                     throw new AssertException(format("All types within a GraphQL schema must have unique names. No two provided types may have the same name.\n" +
 *                                     "No provided type may have a name which conflicts with any built in types (including Scalar and Introspection types).\n" +
 *                                     "You have redefined the type '%s' from being a '%s' to a '%s'",
 *                             type.getName(), existingType.getClass().getSimpleName(), type.getClass().getSimpleName()));
 *                 }
 *         }
 *     }
 * </pre>
 *
 * The bug is that custom scalars {@code (!ScalarInfo.isStandardScalar(scalar) &&
 * !ScalarInfo.isGraphqlSpecifiedScalar(scalar)) } have components added by the schema generator, so
 * when custom scalar implementations are initially added, either through RuntimeWiring directly or
 * through a WiringFactory passed into RuntimeWiring, and later transformed by the SchemaGenerator,
 * their hashcodes are no longer the same. When the types are checked for uniqueness using object
 * reference, they fail this test and the schema generator does not complete
 *
 * <p>The object reference comparison {@code (existingType != type) } means the hashcode cannot be
 * different or schema generation fails, so this scalar wiring factory puts the original reference
 * back through schema directive wiring since this appears to be the only way to work aroundt this
 * issue.
 */
@Immutable
public interface CustomScalarFieldDefDirectiveWiringProvider
    extends FocusedDirectiveWiringProvider<CustomScalarFieldWiring, GraphQLFieldDefinition> {

  static CustomScalarFieldDefDirectiveWiringProvider newInstance() {
    Map<String, CustomScalarFieldWiring> customScalarFieldWiringMap =
        GraphQLScalarsSupport.extendedScalarsMap().entrySet().stream()
            .map(
                entry ->
                    Pair.create(
                        entry.getKey(),
                        CustomScalarFieldWiringImpl.builder()
                            .startingScalarType(entry.getValue())
                            .build()))
            .collect(Collectors.toMap(Pair::first, Pair::second));
    return CustomScalarFieldDefDirectiveWiringProviderImpl.builder()
        .putAllCustomScalarTypeNameToFieldWiring(customScalarFieldWiringMap)
        .build();
  }

  Map<String, CustomScalarFieldWiring> customScalarTypeNameToFieldWiring();

  @Override
  default Class<GraphQLFieldDefinition> wiringElementType() {
    return GraphQLFieldDefinition.class;
  }

  //  @Override
  //  default Function<SchemaDirectiveWiringEnvironment, Optional<GraphQLFieldDefinition>>
  //      retrieveWiringElementFromSchemaDirectiveWiringEnvironment() {
  //    return environment ->
  //        Optional.of(environment.getElement())
  //            .filter(
  //                graphQLDirectiveContainer ->
  //                    graphQLDirectiveContainer instanceof GraphQLFieldDefinition)
  //            .map(GraphQLFieldDefinition.class::cast);
  //  }

  @Override
  default Function<GraphQLFieldDefinition, Optional<CustomScalarFieldWiring>>
      retrieveFocusedDirectiveWiringForWiringElement() {
    return graphQLFieldDefinition ->
        Optional.of(graphQLFieldDefinition.getType())
            .filter(graphQLOutputType -> graphQLOutputType instanceof GraphQLScalarType)
            .map(GraphQLScalarType.class::cast)
            .map(
                graphQLScalarType ->
                    customScalarTypeNameToFieldWiring().get(graphQLScalarType.getName()));
  }
}
