package io.github.anticipasean.girakkagraph.protocol.graphql.domain.operator.impl;

import akka.japi.Pair;
import com.google.common.collect.ImmutableSet;
import io.github.anticipasean.girakkagraph.protocol.graphql.domain.operator.GraphQLArgumentOperator;
import io.github.anticipasean.girakkagraph.protocol.graphql.domain.operator.GraphQLArgumentOperatorFactory;
import io.github.anticipasean.girakkagraph.protocol.graphql.domain.operator.GraphQLArgumentOperatorImpl;
import io.github.anticipasean.girakkagraph.protocol.graphql.domain.operator.GraphQLArgumentOperatorImpl.Builder;
import io.github.anticipasean.girakkagraph.protocol.graphql.domain.scalar.GraphQLScalarsSupport;
import io.github.anticipasean.girakkagraph.protocol.model.domain.arity.Arity;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operator.ModelOperator;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operator.Operator;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operator.OperatorDatabase;
import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.filter.OperatorSupplier;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLScalarType;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class GraphQLArgumentOperatorFactoryImpl implements GraphQLArgumentOperatorFactory {
  private final OperatorDatabase operatorDatabase;
  private final Map<String, GraphQLScalarType> scalarsMap;
  private final Map<String, Set<Class<?>>> coercibleJavaTypeSetMap;
  private final List<ModelOperator> modelOperators;

  public GraphQLArgumentOperatorFactoryImpl(OperatorDatabase operatorDatabase) {
    this.operatorDatabase =
        Objects.requireNonNull(
            operatorDatabase,
            String.format(
                "operator database for %s cannot be null",
                GraphQLArgumentOperatorFactoryImpl.class));
    this.scalarsMap = GraphQLScalarsSupport.allScalarsMap();
    this.coercibleJavaTypeSetMap =
        GraphQLScalarsSupport.graphQLScalarTypeNameToCoercibleJavaTypeSetMap();
    this.modelOperators = operatorDatabase.getModelOperatorRepository().findAll().fetch();
  }

  @Override
  public OperatorDatabase operatorDatabase() {
    return operatorDatabase;
  }

  @Override
  public Set<GraphQLArgumentOperator> createGraphQLArgumentOperatorSetFromOperatorDatabase() {
    ConcurrentMap<String, Builder> argumentNameArgumentOperatorBuilderMap =
        createConcurrentMapOfArgumentNamesToArgumentOperatorBuilders();
    for (GraphQLScalarType graphQLScalarType : new HashSet<>(scalarsMap.values())) {
      updateSupportedGraphQLTypesOnArgumentOperatorBuildersForScalarType(
          argumentNameArgumentOperatorBuilderMap, graphQLScalarType);
      updateGraphQLArgumentOperatorSpecificUpdaterFunctionOnArgumentOperatorBuildersForScalarType(
          argumentNameArgumentOperatorBuilderMap, graphQLScalarType);
    }
    return buildAllGraphQLArgumentOperatorsInArgumentNameArgumentBuildersMap(
        argumentNameArgumentOperatorBuilderMap);
  }

  private ConcurrentMap<String, Builder>
      createConcurrentMapOfArgumentNamesToArgumentOperatorBuilders() {
    return modelOperators.stream()
        .map(ModelOperator::operatorSupplier)
        .map(OperatorSupplier::operator)
        .map(
            operator ->
                Pair.create(
                    operator.callName(),
                    createGraphQLArgumentOperatorBuilderWithInitialOperatorParameters(operator)))
        .collect(Collectors.toConcurrentMap(Pair::first, Pair::second));
  }

  private Builder createGraphQLArgumentOperatorBuilderWithInitialOperatorParameters(
      Operator<?, ?> operator) {
    return GraphQLArgumentOperatorImpl.builder()
        .argumentName(operator.callName())
        .arity(
            operator.functionalParameterRestrictions().isEmpty()
                ? operator.arity()
                : Arity.operandCountToArityMap()
                    .getOrDefault(operator.arity().operandCount() + 1, operator.arity()));
  }

  private void updateSupportedGraphQLTypesOnArgumentOperatorBuildersForScalarType(
      ConcurrentMap<String, Builder> argumentNameArgumentOperatorBuilderMap,
      GraphQLScalarType graphQLScalarType) {
    modelOperators.stream()
        .filter(
            modelOperator ->
                modelOperator.operatesOnAtLeastOneOfTheseFieldTypes(
                    coercibleJavaTypeSetMap.get(graphQLScalarType.getName())))
        .distinct()
        .map(ModelOperator::operatorCallName)
        .forEach(
            updateSupportedGraphQLTypesSetOnCorrespondingArgumentOperatorBuildersInMap(
                argumentNameArgumentOperatorBuilderMap, graphQLScalarType));
  }

  private Consumer<String>
      updateSupportedGraphQLTypesSetOnCorrespondingArgumentOperatorBuildersInMap(
          ConcurrentMap<String, Builder> argumentNameArgumentOperatorBuilderMap,
          GraphQLScalarType graphQLScalarType) {
    return callName -> {
      argumentNameArgumentOperatorBuilderMap.compute(
          callName,
          (cName, builder) -> builder.addSupportedGraphQLTypeName(graphQLScalarType.getName()));
    };
  }

  private void
      updateGraphQLArgumentOperatorSpecificUpdaterFunctionOnArgumentOperatorBuildersForScalarType(
          ConcurrentMap<String, Builder> argumentNameArgumentOperatorBuilderMap,
          GraphQLScalarType graphQLScalarType) {
    modelOperators.stream()
        .filter(
            checkWhetherModelOperatorHasFunctionalParameterTypeRestrictionMatchingScalarType(
                graphQLScalarType))
        .forEach(
            updateArgumentOperatorBuilderWithGraphQLArgumentUpdaterFunction(
                argumentNameArgumentOperatorBuilderMap, graphQLScalarType));
  }

  private Consumer<ModelOperator> updateArgumentOperatorBuilderWithGraphQLArgumentUpdaterFunction(
      ConcurrentMap<String, Builder> argumentNameArgumentOperatorBuilderMap,
      GraphQLScalarType graphQLScalarType) {
    return modelOperator -> {
      argumentNameArgumentOperatorBuilderMap.compute(
          modelOperator.operatorCallName(),
          (cName, builder) ->
              builder.onApplyOperatorSpecificChangesToGraphQLArgument(
                  createModelOperatorFunctionalParameterBasedGraphQLArgumentUpdaterFunction(
                      graphQLScalarType)));
    };
  }

  private Predicate<ModelOperator>
      checkWhetherModelOperatorHasFunctionalParameterTypeRestrictionMatchingScalarType(
          GraphQLScalarType graphQLScalarType) {
    return modelOperator ->
        modelOperator.operatesOnAtLeastOneOfTheseFunctionalParameterTypes(
            GraphQLScalarsSupport.graphQLScalarTypeNameToCoercibleJavaTypeSetMap()
                .get(graphQLScalarType.getName()));
  }

  private UnaryOperator<GraphQLArgument>
      createModelOperatorFunctionalParameterBasedGraphQLArgumentUpdaterFunction(
          GraphQLScalarType graphQLScalarType) {
    return graphQLArgument -> {
      return GraphQLArgument.newArgument(graphQLArgument).type(graphQLScalarType).build();
    };
  }

  private Set<GraphQLArgumentOperator>
      buildAllGraphQLArgumentOperatorsInArgumentNameArgumentBuildersMap(
          ConcurrentMap<String, Builder> argumentNameArgumentOperatorBuilderMap) {
    ImmutableSet.Builder<GraphQLArgumentOperator> graphQLArgumentOperatorSetBuilder =
        ImmutableSet.<GraphQLArgumentOperator>builder();
    for (Entry<String, Builder> argumentNameArgumentOperatorBuilder :
        argumentNameArgumentOperatorBuilderMap.entrySet()) {
      try {
        GraphQLArgumentOperator graphQLArgumentOperator =
            argumentNameArgumentOperatorBuilder.getValue().build();
        graphQLArgumentOperatorSetBuilder.add(graphQLArgumentOperator);
      } catch (IllegalStateException e) {
        throw new IllegalArgumentException(
            String.format(
                "an error was thrown when attempting to build the "
                    + "graphql_argument_operator for [ argumentName: %s ]: one of "
                    + "the required values is likey missing",
                argumentNameArgumentOperatorBuilder.getKey()),
            e);
      }
    }
    return graphQLArgumentOperatorSetBuilder.build();
  }
}
