package io.github.anticipasean.girakkagraph.protocol.model.domain.graph.processor.query;

import io.github.anticipasean.girakkagraph.protocol.model.domain.argument.jpa.CriteriaJpaAggregrationArgument;
import io.github.anticipasean.girakkagraph.protocol.model.domain.argument.jpa.CriteriaJpaArgument;
import io.github.anticipasean.girakkagraph.protocol.model.domain.argument.jpa.CriteriaJpaFunctionalAggregationArgument;
import io.github.anticipasean.girakkagraph.protocol.model.domain.argument.jpa.CriteriaJpaFunctionalOrderArgument;
import io.github.anticipasean.girakkagraph.protocol.model.domain.argument.jpa.CriteriaJpaOrderArgument;
import io.github.anticipasean.girakkagraph.protocol.model.domain.argument.jpa.CriteriaJpaPredicateArgument;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.BasicOrderArgumentEdge;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.BasicOrderArgumentEdgeImpl;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.FunctionalAggregationArgumentEdgeImpl;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.FunctionalOrderArgumentEdgeImpl;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.JpaCriteriaArgumentEdge;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.SelectionAggregationArgumentEdge;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.SelectionAggregationArgumentEdgeImpl;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.SelectionPredicateArgumentEdge;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.SelectionPredicateArgumentEdgeImpl;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.vertex.BasicAttributeVertex;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.ModelPath;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.ModelPathImpl;
import io.github.anticipasean.girakkagraph.typematcher.TypeMatcher;
import java.util.function.BiFunction;

interface ArgumentToArgumentEdgeMapper
    extends BiFunction<BasicAttributeVertex, CriteriaJpaArgument<?>, JpaCriteriaArgumentEdge> {

  default SelectionAggregationArgumentEdge
      mapCriteriaJpaAggregationArgumentToAggregrationArgumentEdge(
          CriteriaJpaAggregrationArgument criteriaJpaAggregrationArgument,
          BasicAttributeVertex basicAttributeVertex) {
    return SelectionAggregationArgumentEdgeImpl.builder()
        .parentPath(basicAttributeVertex.vertexPath())
        .childPath(
            extractArgumentParameterPathFromCriteriaJpaArgumentAndBasicAttributeVertex(
                criteriaJpaAggregrationArgument, basicAttributeVertex))
        .aggregationArgument(criteriaJpaAggregrationArgument)
        .build();
  }

  default BasicOrderArgumentEdge mapCriteriaJpaOrderingArgumentToOrderingArgumentEdge(
      CriteriaJpaOrderArgument criteriaJpaOrderArgument,
      BasicAttributeVertex basicAttributeVertex) {
    return BasicOrderArgumentEdgeImpl.builder()
        .parentPath(basicAttributeVertex.vertexPath())
        .childPath(
            extractArgumentParameterPathFromCriteriaJpaArgumentAndBasicAttributeVertex(
                criteriaJpaOrderArgument, basicAttributeVertex))
        .criteriaJpaOrderArgument(criteriaJpaOrderArgument)
        .build();
  }

  default SelectionPredicateArgumentEdge
      mapCriteriaJpaPredicateArgumentToSelectionOrAggregationPredicateArgumentEdge(
          CriteriaJpaPredicateArgument criteriaJpaPredicateArgument,
          BasicAttributeVertex basicAttributeVertex) {
    return SelectionPredicateArgumentEdgeImpl.builder()
        .parentPath(basicAttributeVertex.vertexPath())
        .childPath(
            extractArgumentParameterPathFromCriteriaJpaArgumentAndBasicAttributeVertex(
                criteriaJpaPredicateArgument, basicAttributeVertex))
        .criteriaJpaArgument(criteriaJpaPredicateArgument)
        .build();
  }

  default JpaCriteriaArgumentEdge mapCriteriaJpaFunctionalAggregationArgumentToAppropriateEdge(
      CriteriaJpaFunctionalAggregationArgument criteriaJpaFunctionalAggregationArgument,
      BasicAttributeVertex basicAttributeVertex) {
    return FunctionalAggregationArgumentEdgeImpl.builder()
        .parentPath(basicAttributeVertex.vertexPath())
        .childPath(
            extractArgumentParameterPathFromCriteriaJpaArgumentAndBasicAttributeVertex(
                criteriaJpaFunctionalAggregationArgument, basicAttributeVertex))
        .criteriaJpaFunctionalAggregationArgument(criteriaJpaFunctionalAggregationArgument)
        .build();
  }

  default JpaCriteriaArgumentEdge mapCriteriaJpaFunctionalOrderArgumentToAppropriateEdge(
      CriteriaJpaFunctionalOrderArgument criteriaJpaFunctionalOrderArgument,
      BasicAttributeVertex basicAttributeVertex) {
    return FunctionalOrderArgumentEdgeImpl.builder()
        .parentPath(basicAttributeVertex.vertexPath())
        .childPath(
            extractArgumentParameterPathFromCriteriaJpaArgumentAndBasicAttributeVertex(
                criteriaJpaFunctionalOrderArgument, basicAttributeVertex))
        .criteriaJpaFunctionalOrderArgument(criteriaJpaFunctionalOrderArgument)
        .build();
  }

  default ModelPath extractArgumentParameterPathFromCriteriaJpaArgumentAndBasicAttributeVertex(
      CriteriaJpaArgument<?> criteriaJpaArgument, BasicAttributeVertex basicAttributeVertex) {
    return ((ModelPathImpl) basicAttributeVertex.vertexPath())
        .withRawArguments(extractRawArgument(criteriaJpaArgument));
  }

  default String extractRawArgument(CriteriaJpaArgument<?> criteriaJpaArgument) {
    String operatorCallName = criteriaJpaArgument.operator().callName();
    return String.join("=", operatorCallName, ModelPath.PARAMETER);
  }

  static ArgumentToArgumentEdgeMapper newInstance() {
   return Mapper.INSTANCE.argumentToArgumentEdgeMapper;
  }

  enum Mapper {
    INSTANCE(new ArgumentToArgumentEdgeMapper() {});
    private final ArgumentToArgumentEdgeMapper argumentToArgumentEdgeMapper;

    Mapper(
        ArgumentToArgumentEdgeMapper argumentToArgumentEdgeMapper) {
      this.argumentToArgumentEdgeMapper = argumentToArgumentEdgeMapper;
    }
  }

  @Override
  default JpaCriteriaArgumentEdge apply(
      BasicAttributeVertex basicAttributeVertex, CriteriaJpaArgument<?> criteriaJpaArgument) {
    return TypeMatcher.whenTypeOf(criteriaJpaArgument)
        .is(CriteriaJpaAggregrationArgument.class)
        .thenApply(
            criteriaJpaAggregrationArgument ->
                (JpaCriteriaArgumentEdge)
                    mapCriteriaJpaAggregationArgumentToAggregrationArgumentEdge(
                        criteriaJpaAggregrationArgument, basicAttributeVertex))
        .is(CriteriaJpaOrderArgument.class)
        .thenApply(
            criteriaJpaOrderArgument ->
                mapCriteriaJpaOrderingArgumentToOrderingArgumentEdge(
                    criteriaJpaOrderArgument, basicAttributeVertex))
        .is(CriteriaJpaPredicateArgument.class)
        .thenApply(
            criteriaJpaPredicateArgument ->
                mapCriteriaJpaPredicateArgumentToSelectionOrAggregationPredicateArgumentEdge(
                    criteriaJpaPredicateArgument, basicAttributeVertex))
        .is(CriteriaJpaFunctionalAggregationArgument.class)
        .thenApply(
            criteriaJpaFunctionalAggregationArgument ->
                mapCriteriaJpaFunctionalAggregationArgumentToAppropriateEdge(
                    criteriaJpaFunctionalAggregationArgument, basicAttributeVertex))
        .is(CriteriaJpaFunctionalOrderArgument.class)
        .thenApply(
            criteriaJpaFunctionalOrderArgument ->
                mapCriteriaJpaFunctionalOrderArgumentToAppropriateEdge(
                    criteriaJpaFunctionalOrderArgument, basicAttributeVertex))
        .orElseThrow(
            () ->
                new IllegalStateException(
                    String.format(
                        "criteria jpa argument type for argument [ %s ] has not been handled in argument edge creation logic",
                        criteriaJpaArgument)));
  }
}
