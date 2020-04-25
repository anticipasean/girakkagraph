package io.github.anticipasean.girakkagraph.protocol.model.domain.operator.jpa;

import akka.japi.Pair;
import com.google.common.collect.ImmutableSet;
import io.github.anticipasean.girakkagraph.protocol.model.domain.arity.Arity;
import io.github.anticipasean.girakkagraph.protocol.model.domain.filter.Aggregation;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operandset.GenericRestrictions;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operandset.jpa.JpaCriteriaOperandSet;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operation.Operation;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operator.Restriction;
import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.ExpressionSupplier;
import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.FunctionalGroupByExpressionSupplier;
import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.FunctionalGroupByExpressionSupplier.IndexedExpressionSupplier;
import java.util.Set;
import java.util.function.Function;
import javax.persistence.criteria.Expression;

public interface JpaAggregationOperator {

  //  Predicate<JpaCriteriaOperandSet> isNumericExpression =
  //      unaryOperandSet ->
  //          Number.class.isAssignableFrom(
  //              unaryOperandSet
  //                  .operands()
  //                  .get(0)
  //                  .valueSupplier()
  //                  .expression()
  //                  .getJavaType());

  enum Unary implements JpaCriteriaOperator<ExpressionSupplier>, Aggregation {

    /**
     * Create an aggregate expression applying the avg operation.
     *
     * @param x expression representing input value to avg operation
     * @return avg expression
     */
    //    <N extends Number> Expression<Double> avg(Expression<N> x);
    AVG("avg") {

      @Override
      public Operation<JpaCriteriaOperandSet, ExpressionSupplier> operation() {
        return jpaCriteriaUnaryOperandSet -> {
          Expression<Double> avgExpression =
              jpaCriteriaUnaryOperandSet
                  .processingContext()
                  .criteriaBuilder()
                  .avg(jpaCriteriaUnaryOperandSet.operands().get(0).valueSupplier().expression());
          return () -> avgExpression;
        };
      }

      @Override
      public Set<Restriction<JpaCriteriaOperandSet>> specificRestrictions() {
        return ImmutableSet.of(GenericRestrictions.allOperandsMustRepresentTypeNumber());
      }
    },

    /**
     * Create an aggregate expression applying the sum operation.
     *
     * @param x expression representing input value to sum operation
     * @return sum expression
     */
    //    <N extends Number> Expression<N> sum(Expression<N> x);
    SUM("sum") {
      @Override
      public Operation<JpaCriteriaOperandSet, ExpressionSupplier> operation() {
        return jpaCriteriaUnaryOperandSet -> {
          Expression<Number> sumExpression =
              jpaCriteriaUnaryOperandSet
                  .processingContext()
                  .criteriaBuilder()
                  .sum(jpaCriteriaUnaryOperandSet.operands().get(0).valueSupplier().expression());
          return () -> sumExpression;
        };
      }

      @Override
      public Set<Restriction<JpaCriteriaOperandSet>> specificRestrictions() {
        return ImmutableSet.of(GenericRestrictions.allOperandsMustRepresentTypeNumber());
      }
    },

    /**
     * Create an aggregate expression applying the sum operation to an Integer-valued expression,
     * returning a Long result.
     *
     * @param x expression representing input value to sum operation
     * @return sum expression
     */
    //    Expression<Long> sumAsLong(Expression<Integer> x);
    //    SUM_AS_LONG("sumAsLong", Arity.UNARY, ImmutableList.of(NumericFieldOperand.class)) {},

    /**
     * Create an aggregate expression applying the sum operation to a Float-valued expression,
     * returning a Double result.
     *
     * @param x expression representing input value to sum operation
     * @return sum expression
     */
    //    Expression<Double> sumAsDouble(Expression<Float> x);
    //    SUM_AS_DOUBLE("sumAsDouble", Arity.UNARY, ImmutableList.of(NumericFieldOperand.class)) {},

    /**
     * Create an aggregate expression applying the numerical max operation.
     *
     * @param x expression representing input value to max operation
     * @return max expression
     */
    //    <N extends Number> Expression<N> max(Expression<N> x);
    //    MAX("max", Arity.UNARY, ImmutableList.of(NumericFieldOperand.class)) {},

    /**
     * Create an aggregate expression applying the numerical min operation.
     *
     * @param x expression representing input value to min operation
     * @return min expression
     */
    //    <N extends Number> Expression<N> min(Expression<N> x);
    //    MIN("min", Arity.UNARY, ImmutableList.of(NumericFieldOperand.class)) {},

    /**
     * Create an aggregate expression for finding the greatest of the values (strings, dates, etc).
     *
     * @param x expression representing input value to greatest operation
     * @return greatest expression
     */
    //    <X extends Comparable<? super X>> Expression<X> greatest(Expression<X> x);
    //    GREATEST("greatest", Arity.UNARY, ImmutableList.of(ComparableFieldOperand.class)) {},

    /**
     * Create an aggregate expression for finding the least of the values (strings, dates, etc).
     *
     * @param x expression representing input value to least operation
     * @return least expression
     */
    //    <X extends Comparable<? super X>> Expression<X> least(Expression<X> x);
    //    LEAST("least", Arity.UNARY, ImmutableList.of(ComparableFieldOperand.class)) {},

    //    BiFunction<CriteriaBuilder, List<Operand>, ReturnValue>
    // includeGeneralChecksBeforeExecution(
    //        BiFunction<CriteriaBuilder, List<Operand>, ReturnValue> coreFunc) {
    //      return (criteriaBuilder, operandList) -> {
    //        Operator.checkArityOfOperandArguments.accept(arity, operandList);
    //        Operator.checkOperandsMatchExpectedTypes.accept(expectedOperandTypes, operandList);
    //        return coreFunc.apply(criteriaBuilder, operandList);
    //      };
    //    }

    //    <X extends Comparable<? super X>>
    //        Expression<X> getUnaryOperatorComparableFieldExpressionFromOperand(
    //            List<Operand> operandList) {
    //      ComparableFieldOperand operand = (ComparableFieldOperand) operandList.get(0);
    //      return operand.expressionSupplier().expression();
    //    }
    //  }

    /**
     * Create an aggregate expression applying the count operation.
     *
     * @param x expression representing input value to count operation
     * @return count expression
     */
    //    Expression<Long> count(Expression<?> x);
    COUNT("count") {
      @Override
      public Operation<JpaCriteriaOperandSet, ExpressionSupplier> operation() {
        return jpaCriteriaUnaryOperandSet -> {
          Expression<Long> countExpression =
              jpaCriteriaUnaryOperandSet
                  .processingContext()
                  .criteriaBuilder()
                  .count(jpaCriteriaUnaryOperandSet.operands().get(0).valueSupplier().expression());
          return () -> countExpression;
        };
      }

      @Override
      public Set<Restriction<JpaCriteriaOperandSet>> specificRestrictions() {
        return ImmutableSet.of();
      }
    },

    /**
     * Create an aggregate expression applying the count distinct operation.
     *
     * @param x expression representing input value to count distinct operation
     * @return count distinct expression
     */
    //    Expression<Long> countDistinct(Expression<?> x);
    COUNT_DISTINCT("countDistinct") {
      @Override
      public Operation<JpaCriteriaOperandSet, ExpressionSupplier> operation() {
        return jpaCriteriaUnaryOperandSet -> {
          Expression<Long> countDistinctExpression =
              jpaCriteriaUnaryOperandSet
                  .processingContext()
                  .criteriaBuilder()
                  .countDistinct(
                      jpaCriteriaUnaryOperandSet.operands().get(0).valueSupplier().expression());
          return () -> countDistinctExpression;
        };
      }

      @Override
      public Set<Restriction<JpaCriteriaOperandSet>> specificRestrictions() {
        return ImmutableSet.of();
      }
    };
    private final String callName;
    private final Arity arity;

    Unary(String callName) {
      this.callName = callName;
      this.arity = Arity.UNARY;
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

  enum UnaryFunctional implements JpaCriteriaOperator<FunctionalGroupByExpressionSupplier>, Aggregation {
    GROUP {
      @Override
      public Arity arity() {
        return Arity.UNARY;
      }

      @Override
      public String callName() {
        return "group";
      }


      @Override
      public Set<Restriction<JpaCriteriaOperandSet>> specificRestrictions() {
        return ImmutableSet.of();
      }

      @Override
      public Set<Restriction<?>> functionalParameterRestrictions() {
        return ImmutableSet.of(GenericRestrictions.functionalParameterIsOfType(Integer.TYPE));
      }

      @Override
      public Operation<JpaCriteriaOperandSet, FunctionalGroupByExpressionSupplier> operation() {
        return jpaCriteriaOperandSet -> {
          ExpressionSupplier fieldExpressionSupplier = jpaCriteriaOperandSet.operands().get(0)
              .valueSupplier();
          Function<Integer, IndexedExpressionSupplier> pairingFunction = integer -> {
            return () -> Pair.create(integer, fieldExpressionSupplier);
          };
          return () -> pairingFunction;
        };
      }
    }
  }
}
