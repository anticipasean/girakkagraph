package io.github.anticipasean.girakkagraph.protocol.model.domain.operator.jpa;

import com.google.common.collect.ImmutableSet;
import io.github.anticipasean.girakkagraph.protocol.model.domain.arity.Arity;
import io.github.anticipasean.girakkagraph.protocol.model.domain.filter.Ordering;
import io.github.anticipasean.girakkagraph.protocol.model.domain.filter.Ordering.Order;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operandset.GenericRestrictions;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operandset.jpa.JpaCriteriaOperandSet;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operation.Operation;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operator.Restriction;
import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.ExpressionSupplier;
import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.FunctionalOrderSupplier;
import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.OrderSupplier;
import graphql.schema.GraphQLEnumType;
import java.util.Set;
import java.util.function.Function;

public interface JpaOrderingOperator {

  static GraphQLEnumType orderingGraphQLEnums =
      GraphQLEnumType.newEnum()
          .name("Order")
          .value("asc", Order.ASC)
          .value("desc", Order.DESC)
          .build();

  enum UnaryFunctional implements JpaCriteriaOperator<FunctionalOrderSupplier>, Ordering {
    ORDER("order") {

      @Override
      public Set<Restriction<JpaCriteriaOperandSet>> specificRestrictions() {
        return Unary.ASC.specificRestrictions();
      }

      @Override
      public Set<Restriction<?>> functionalParameterRestrictions() {
        return ImmutableSet.<Restriction<?>>of(GenericRestrictions.functionalParameterIsOfTypeString());
      }

      @Override
      public Operation<JpaCriteriaOperandSet, FunctionalOrderSupplier> operation() {
        return operandSet -> {
          ExpressionSupplier fieldExpressionSupplier = operandSet.operands().get(0).valueSupplier();
          Function<String, OrderSupplier> valueFunction = s -> {
            if(s.matches("(?i)desc.*")){
              return () -> operandSet.processingContext().criteriaBuilder().desc(fieldExpressionSupplier.expression());
            } else {
              return () -> operandSet.processingContext().criteriaBuilder().asc(fieldExpressionSupplier.expression());
            }
          };
          return () -> valueFunction;
        };
      }
    };

    private final String callName;

    UnaryFunctional(String callName) {
      this.callName = callName;
    }

    @Override
    public String callName() {
      return callName;
    }

    @Override
    public Arity arity() {
      return Arity.UNARY;
    }
  }

  enum Unary implements JpaCriteriaOperator<OrderSupplier>, Ordering {
    /**
     * Create an ordering by the ascending value of the expression.
     *
     * @param x expression used to define the ordering
     * @return ascending ordering corresponding to the expression
     */
    //    Order asc(Expression<?> x);
    ASC("asc") {
      @Override
      public Operation<JpaCriteriaOperandSet, OrderSupplier> operation() {
        return jpaCriteriaUnaryOperandSet -> {
          javax.persistence.criteria.Order ascOrder =
              jpaCriteriaUnaryOperandSet
                  .processingContext()
                  .criteriaBuilder()
                  .asc(jpaCriteriaUnaryOperandSet.operands().get(0).valueSupplier().expression());
          return () -> ascOrder;
        };
      }
    },

    /**
     * Create an ordering by the descending value of the expression.
     *
     * @param x expression used to define the ordering
     * @return descending ordering corresponding to the expression
     */
    //    Order desc(Expression<?> x);

    DESC("desc") {
      @Override
      public Operation<JpaCriteriaOperandSet, OrderSupplier> operation() {
        return jpaCriteriaUnaryOperandSet -> {
          javax.persistence.criteria.Order descOrder =
              jpaCriteriaUnaryOperandSet
                  .processingContext()
                  .criteriaBuilder()
                  .desc(jpaCriteriaUnaryOperandSet.operands().get(0).valueSupplier().expression());
          return () -> descOrder;
        };
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
    public Set<Restriction<JpaCriteriaOperandSet>> specificRestrictions() {
      return ImmutableSet.of(GenericRestrictions.operandSetNotEmpty());
    }

    @Override
    public Arity arity() {
      return arity;
    }
  }
}

/*
{
      @Override
      public BiFunction<CriteriaBuilder, List<Operand>, ReturnValue>
          criteriaOperatorMappingFunction() {
        return (criteriaBuilder, operandList) -> {
          checkArityOfOperandArguments.accept(arity(), operandList);
          checkOperandsMatchExpectedTypes.accept(expectedOperandTypes(), operandList);
          FieldOperand operand = (FieldOperand) operandList.get(0);
          Expression<?> expression = operand.expressionSupplier().expression();
          Order ascOrder = criteriaBuilder.asc(expression);
          return OrderReturnValueImpl.builder().criteriaOrder(ascOrder).build();
        };
      }
    }

{
      @Override
      public BiFunction<CriteriaBuilder, List<Operand>, ReturnValue>
          criteriaOperatorMappingFunction() {
        return (criteriaBuilder, operandList) -> {
          checkArityOfOperandArguments.accept(arity(), operandList);
          checkOperandsMatchExpectedTypes.accept(expectedOperandTypes(), operandList);
          FieldOperand operand = (FieldOperand) operandList.get(0);
          Expression<?> expression = operand.expressionSupplier().expression();
          Order ascOrder = criteriaBuilder.desc(expression);
          return OrderReturnValueImpl.builder().criteriaOrder(ascOrder).build();
        };
      }
    };
* */
