package io.github.anticipasean.girakkagraph.protocol.model.domain.operator.jpa;

import com.google.common.collect.ImmutableSet;
import io.github.anticipasean.girakkagraph.protocol.model.domain.arity.Arity;
import io.github.anticipasean.girakkagraph.protocol.model.domain.filter.Comparison;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operandset.GenericRestrictions;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operandset.jpa.JpaCriteriaOperandSet;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operation.Operation;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operator.Restriction;
import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.ExpressionSupplier;
import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.PredicateSupplier;
import java.util.Set;
import javax.persistence.criteria.Predicate;

public interface JpaComparativeOperator {

  static boolean expressionSuppliedRepresentsTypeOrSubTypeOf(
      ExpressionSupplier expressionSupplier, Class<?> type) {
    return type.isAssignableFrom(expressionSupplier.expression().getJavaType());
  }

  enum Binary implements JpaCriteriaPredicateOperator, Comparison {

    EQUAL_TO_EXPRESSION("eq") {
      @Override
      public Set<Restriction<JpaCriteriaOperandSet>> specificRestrictions() {
        return ImmutableSet.of();
      }

      @Override
      public Operation<JpaCriteriaOperandSet, PredicateSupplier> operation() {
        return jpaCriteriaBinaryOperandSet -> {
          ExpressionSupplier fieldExpressionSupplier =
              jpaCriteriaBinaryOperandSet.operands().get(0).valueSupplier();
          ExpressionSupplier valueExpressionSupplier =
              jpaCriteriaBinaryOperandSet.operands().get(1).valueSupplier();
          return () ->
              jpaCriteriaBinaryOperandSet
                  .processingContext()
                  .criteriaBuilder()
                  .equal(
                      fieldExpressionSupplier.expression(), valueExpressionSupplier.expression());
        };
      }
    },

    NOT_EQUAL_TO_EXPRESSION("not_eq") {
      @Override
      public Set<Restriction<JpaCriteriaOperandSet>> specificRestrictions() {
        return ImmutableSet.of();
      }

      @Override
      public Operation<JpaCriteriaOperandSet, PredicateSupplier> operation() {
        return jpaCriteriaOperandSet -> {
          ExpressionSupplier fieldExpressionSupplier =
              jpaCriteriaOperandSet.operands().get(0).valueSupplier();
          ExpressionSupplier valueExpressionSupplier =
              jpaCriteriaOperandSet.operands().get(1).valueSupplier();
          return () ->
              jpaCriteriaOperandSet
                  .processingContext()
                  .criteriaBuilder()
                  .notEqual(
                      fieldExpressionSupplier.expression(), valueExpressionSupplier.expression());
        };
      }
    },

    GREATER_THAN_COMPARABLE_EXPRESSION("gt") {
      @Override
      public Set<Restriction<JpaCriteriaOperandSet>> specificRestrictions() {
        return ImmutableSet.of(GenericRestrictions.allOperandsMustRepresentTypeComparable());
      }

      @Override
      public Operation<JpaCriteriaOperandSet, PredicateSupplier> operation() {
        return jpaCriteriaOperandSet -> {
          ExpressionSupplier fieldExpressionSupplier =
              jpaCriteriaOperandSet.operands().get(0).valueSupplier();
          ExpressionSupplier valueExpressionSupplier =
              jpaCriteriaOperandSet.operands().get(1).valueSupplier();
          if (expressionSuppliedRepresentsTypeOrSubTypeOf(fieldExpressionSupplier, Number.class)) {
            return () ->
                jpaCriteriaOperandSet
                    .processingContext()
                    .criteriaBuilder()
                    .gt(fieldExpressionSupplier.expression(), valueExpressionSupplier.expression());
          } else {
            return () ->
                jpaCriteriaOperandSet
                    .processingContext()
                    .criteriaBuilder()
                    .greaterThan(
                        fieldExpressionSupplier.expression(), valueExpressionSupplier.expression());
          }
        };
      }
    },

    GREATER_THAN_OR_EQUAL_TO_COMPARABLE_EXPRESSION("gte") {
      @Override
      public Set<Restriction<JpaCriteriaOperandSet>> specificRestrictions() {
        return ImmutableSet.of(GenericRestrictions.allOperandsMustRepresentTypeComparable());
      }

      @Override
      public Operation<JpaCriteriaOperandSet, PredicateSupplier> operation() {
        return jpaCriteriaOperandSet -> {
          ExpressionSupplier fieldExpressionSupplier =
              jpaCriteriaOperandSet.operands().get(0).valueSupplier();
          ExpressionSupplier valueExpressionSupplier =
              jpaCriteriaOperandSet.operands().get(1).valueSupplier();
          if (expressionSuppliedRepresentsTypeOrSubTypeOf(fieldExpressionSupplier, Number.class)) {
            return () ->
                jpaCriteriaOperandSet
                    .processingContext()
                    .criteriaBuilder()
                    .ge(fieldExpressionSupplier.expression(), valueExpressionSupplier.expression());
          } else {
            return () ->
                jpaCriteriaOperandSet
                    .processingContext()
                    .criteriaBuilder()
                    .greaterThanOrEqualTo(
                        fieldExpressionSupplier.expression(), valueExpressionSupplier.expression());
          }
        };
      }
    },

     LESS_THAN_COMPARABLE_EXPRESSION("lt") {
      @Override
      public Set<Restriction<JpaCriteriaOperandSet>> specificRestrictions() {
        return ImmutableSet.of(GenericRestrictions.allOperandsMustRepresentTypeComparable());
      }

      @Override
      public Operation<JpaCriteriaOperandSet, PredicateSupplier> operation() {
        return jpaCriteriaOperandSet -> {
          ExpressionSupplier fieldExpressionSupplier =
              jpaCriteriaOperandSet.operands().get(0).valueSupplier();
          ExpressionSupplier valueExpressionSupplier =
              jpaCriteriaOperandSet.operands().get(1).valueSupplier();
          if (expressionSuppliedRepresentsTypeOrSubTypeOf(fieldExpressionSupplier, Number.class)) {
            return () ->
                jpaCriteriaOperandSet
                    .processingContext()
                    .criteriaBuilder()
                    .lt(fieldExpressionSupplier.expression(), valueExpressionSupplier.expression());
          } else {
            return () ->
                jpaCriteriaOperandSet
                    .processingContext()
                    .criteriaBuilder()
                    .lessThan(
                        fieldExpressionSupplier.expression(), valueExpressionSupplier.expression());
          }
        };
      }
    },

    LESS_THAN_OR_EQUAL_TO_COMPARABLE_EXPRESSION("lte") {
      @Override
      public Set<Restriction<JpaCriteriaOperandSet>> specificRestrictions() {
        return ImmutableSet.of(GenericRestrictions.allOperandsMustRepresentTypeComparable());
      }

      @Override
      public Operation<JpaCriteriaOperandSet, PredicateSupplier> operation() {
        return jpaCriteriaOperandSet -> {
          ExpressionSupplier fieldExpressionSupplier =
              jpaCriteriaOperandSet.operands().get(0).valueSupplier();
          ExpressionSupplier valueExpressionSupplier =
              jpaCriteriaOperandSet.operands().get(1).valueSupplier();
          if (expressionSuppliedRepresentsTypeOrSubTypeOf(fieldExpressionSupplier, Number.class)) {
            return () ->
                jpaCriteriaOperandSet
                    .processingContext()
                    .criteriaBuilder()
                    .le(fieldExpressionSupplier.expression(), valueExpressionSupplier.expression());
          } else {
            return () ->
                jpaCriteriaOperandSet
                    .processingContext()
                    .criteriaBuilder()
                    .lessThanOrEqualTo(
                        fieldExpressionSupplier.expression(), valueExpressionSupplier.expression());
          }
        };
      }
    };


    private final String callName;

    Binary(String callName) {
      this.callName = callName;
    }

    @Override
    public String callName() {
      return callName;
    }

    @Override
    public Arity arity() {
      return Arity.BINARY;
    }
  }

  enum Ternary implements JpaCriteriaPredicateOperator, Comparison {

    BETWEEN_COMPARABLE_EXPRESSION("betw") {

      @Override
      public Operation<JpaCriteriaOperandSet, PredicateSupplier> operation() {
        return jpaCriteriaTernaryOperandSet -> {
          Predicate betweenPredicate =
              jpaCriteriaTernaryOperandSet
                  .processingContext()
                  .criteriaBuilder()
                  .between(
                      jpaCriteriaTernaryOperandSet.operands().get(0).valueSupplier().expression(),
                      jpaCriteriaTernaryOperandSet.operands().get(1).valueSupplier().expression(),
                      jpaCriteriaTernaryOperandSet.operands().get(2).valueSupplier().expression());
          return () -> betweenPredicate;
        };
      }

      @Override
      public Set<Restriction<JpaCriteriaOperandSet>> specificRestrictions() {
        return ImmutableSet.of(GenericRestrictions.allOperandsMustRepresentTypeComparable());
      }
    };

    private final String callName;

    private final Arity arity;

    Ternary(String callName) {
      this.callName = callName;
      this.arity = Arity.TERNARY;
    }

    @Override
    public String callName() {
      return callName;
    }

    @Override
    public Arity arity() {
      return arity;
    }
  }
}
