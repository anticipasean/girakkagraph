package io.github.anticipasean.girakkagraph.protocol.model.domain.graph.processor.query;

import io.github.anticipasean.girakkagraph.protocol.model.domain.argument.jpa.CriteriaJpaAggregrationArgumentImpl;
import io.github.anticipasean.girakkagraph.protocol.model.domain.argument.jpa.CriteriaJpaArgument;
import io.github.anticipasean.girakkagraph.protocol.model.domain.argument.jpa.CriteriaJpaFunctionalAggregationArgumentImpl;
import io.github.anticipasean.girakkagraph.protocol.model.domain.argument.jpa.CriteriaJpaFunctionalOrderArgumentImpl;
import io.github.anticipasean.girakkagraph.protocol.model.domain.argument.jpa.CriteriaJpaPredicateArgumentImpl;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operandset.jpa.JpaCriteriaOperandSet;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operator.jpa.JpaAggregationOperator;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operator.jpa.JpaAggregationOperator.Unary;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operator.jpa.JpaCriteriaOperator;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operator.jpa.JpaCriteriaPredicateOperator;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operator.jpa.JpaOrderingOperator.UnaryFunctional;
import io.github.anticipasean.girakkagraph.typematcher.TypeMatcher;
import java.util.function.BiFunction;
import java.util.function.Function;

interface OperatorOperandSetToJpaCriteriaArgumentMapper
    extends BiFunction<JpaCriteriaOperator<?>, JpaCriteriaOperandSet, CriteriaJpaArgument<?>> {

  static OperatorOperandSetToJpaCriteriaArgumentMapper newInstance() {
    return new OperatorOperandSetToJpaCriteriaArgumentMapper() {
      @Override
      public CriteriaJpaArgument<?> apply(
          JpaCriteriaOperator<?> jpaCriteriaOperator, JpaCriteriaOperandSet jpaCriteriaOperandSet) {
        return OperatorOperandSetToJpaCriteriaArgumentMapper.super.apply(
            jpaCriteriaOperator, jpaCriteriaOperandSet);
      }
    };
  }

  @Override
  default CriteriaJpaArgument<?> apply(
      JpaCriteriaOperator<?> jpaCriteriaOperator, JpaCriteriaOperandSet jpaCriteriaOperandSet) {
    return TypeMatcher.whenTypeOf(jpaCriteriaOperator)
        .is(JpaCriteriaPredicateOperator.class)
        .thenApply(mapToPredicateArgument(jpaCriteriaOperandSet))
        .is(UnaryFunctional.class)
        .thenApply(mapToFunctionalOrderArgument(jpaCriteriaOperandSet))
        .is(Unary.class)
        .thenApply(mapToAggregrationArgument(jpaCriteriaOperandSet))
        .is(JpaAggregationOperator.UnaryFunctional.class)
        .thenApply(mapToFunctionalAggregationArgument(jpaCriteriaOperandSet))
        .orElseThrow(
            () ->
                new IllegalStateException(
                    String.format(
                        "operator: [ %s ]: jpa criteria operator type does not fit into current set handled:"
                            + "predicate-returning, ordering, or aggregative",
                        jpaCriteriaOperator.name())));
  }

  default Function<JpaAggregationOperator.UnaryFunctional, CriteriaJpaArgument<?>>
      mapToFunctionalAggregationArgument(JpaCriteriaOperandSet jpaCriteriaOperandSet) {
    return jpaAggregationOperator ->
        CriteriaJpaFunctionalAggregationArgumentImpl.builder()
            .operator(jpaAggregationOperator)
            .operandSet(jpaCriteriaOperandSet)
            .build();
  }

  default Function<Unary, CriteriaJpaArgument<?>> mapToAggregrationArgument(
      JpaCriteriaOperandSet jpaCriteriaOperandSet) {
    return jpaAggregationOperator ->
        CriteriaJpaAggregrationArgumentImpl.builder()
            .operator(jpaAggregationOperator)
            .operandSet(jpaCriteriaOperandSet)
            .build();
  }

  default Function<UnaryFunctional, CriteriaJpaArgument<?>> mapToFunctionalOrderArgument(
      JpaCriteriaOperandSet jpaCriteriaOperandSet) {
    return jpaOrderingOperator ->
        CriteriaJpaFunctionalOrderArgumentImpl.builder()
            .operator(jpaOrderingOperator)
            .operandSet(jpaCriteriaOperandSet)
            .build();
  }

  default Function<JpaCriteriaPredicateOperator, CriteriaJpaArgument<?>> mapToPredicateArgument(
      JpaCriteriaOperandSet jpaCriteriaOperandSet) {
    return jpaCriteriaPredicateOperator ->
        CriteriaJpaPredicateArgumentImpl.builder()
            .operandSet(jpaCriteriaOperandSet)
            .operator(jpaCriteriaPredicateOperator)
            .build();
  }
}
