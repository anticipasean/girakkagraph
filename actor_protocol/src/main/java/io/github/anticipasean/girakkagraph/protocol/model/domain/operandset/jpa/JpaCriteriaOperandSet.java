package io.github.anticipasean.girakkagraph.protocol.model.domain.operandset.jpa;

import io.github.anticipasean.girakkagraph.protocol.model.domain.arity.ArityRestricted;
import io.github.anticipasean.girakkagraph.protocol.model.domain.context.JpaCriteriaOperatorProcessingContext;
import io.github.anticipasean.girakkagraph.protocol.model.domain.context.JpaCriteriaOperatorProcessingContextImpl;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operand.jpa.JpaExpressionOperand;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operand.jpa.JpaExpressionOperandImpl;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operandset.ContextualOperandSet;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.ParameterExpression;

public interface JpaCriteriaOperandSet
    extends ContextualOperandSet<JpaExpressionOperand, JpaCriteriaOperatorProcessingContext>,
        ArityRestricted {

  static UnaryOperandSetPath ofField(Expression<?> expression) {
    return new Builder().ofField(expression);
  }

  List<JpaExpressionOperand> operands();

  interface StartOperandSetBuildPath {
    UnaryOperandSetPath ofField(Expression<?> expression);
  }

  interface UnaryOperandSetPath {

    BinaryOperandSetPath andValue(ParameterExpression<?> parameterExpression);

    BinaryOperandSetPath andValueType(Class<?> parameterExpressionType);

    JpaCriteriaOperandSet buildUnarySet(CriteriaBuilder criteriaBuilder);
  }

  interface BinaryOperandSetPath {

    TernaryOperandSetPath lastValue(ParameterExpression<?> parameterExpression);

    TernaryOperandSetPath lastValueType(Class<?> parameterExpressionType);

    JpaCriteriaOperandSet buildBinarySet(CriteriaBuilder criteriaBuilder);
  }

  interface TernaryOperandSetPath {

    JpaCriteriaOperandSet buildTernarySet(CriteriaBuilder criteriaBuilder);
  }

  class Builder
      implements StartOperandSetBuildPath,
          UnaryOperandSetPath,
          BinaryOperandSetPath,
          TernaryOperandSetPath {

    private final List<Expression<?>> expressionList;
    private final List<Class<?>> valueTypes;

    Builder() {
      this.expressionList = new ArrayList<>();
      this.valueTypes = new ArrayList<>();
    }

    @Override
    public UnaryOperandSetPath ofField(Expression<?> expression) {
      expressionList.add(expression);
      return this;
    }

    @Override
    public BinaryOperandSetPath andValue(ParameterExpression<?> parameterExpression) {
      expressionList.add(parameterExpression);
      return this;
    }

    @Override
    public BinaryOperandSetPath andValueType(Class<?> parameterExpressionType) {
      valueTypes.add(parameterExpressionType);
      return this;
    }

    @Override
    public JpaCriteriaOperandSet buildUnarySet(CriteriaBuilder criteriaBuilder) {
      JpaCriteriaOperatorProcessingContext context =
          JpaCriteriaOperatorProcessingContextImpl.builder()
              .criteriaBuilder(criteriaBuilder)
              .build();
      List<JpaExpressionOperand> expressionsAsOperands = getExpressionsAsOperands();
      return JpaCriteriaUnaryOperandSetImpl.builder()
          .operands(expressionsAsOperands)
          .processingContext(context)
          .build();
    }

    @Override
    public TernaryOperandSetPath lastValue(ParameterExpression<?> parameterExpression) {
      expressionList.add(parameterExpression);
      return this;
    }

    @Override
    public TernaryOperandSetPath lastValueType(Class<?> parameterExpressionType) {
      valueTypes.add(parameterExpressionType);
      return this;
    }

    @Override
    public JpaCriteriaOperandSet buildBinarySet(CriteriaBuilder criteriaBuilder) {
      JpaCriteriaOperatorProcessingContext context =
          JpaCriteriaOperatorProcessingContextImpl.builder()
              .criteriaBuilder(criteriaBuilder)
              .build();
      addValueTypesAsParameterExpressionsToExpressionsListUsingContext(context);
      List<JpaExpressionOperand> jpaExpressionOperands = getExpressionsAsOperands();
      return JpaCriteriaBinaryOperandSetImpl.builder()
          .operands(jpaExpressionOperands)
          .processingContext(context)
          .build();
    }

    @Override
    public JpaCriteriaOperandSet buildTernarySet(CriteriaBuilder criteriaBuilder) {
      JpaCriteriaOperatorProcessingContext context =
          JpaCriteriaOperatorProcessingContextImpl.builder()
              .criteriaBuilder(criteriaBuilder)
              .build();
      addValueTypesAsParameterExpressionsToExpressionsListUsingContext(context);
      List<JpaExpressionOperand> jpaExpressionOperands = getExpressionsAsOperands();
      return JpaCriteriaTernaryOperandSetImpl.builder()
          .operands(jpaExpressionOperands)
          .processingContext(context)
          .build();
    }

    private BiFunction<JpaCriteriaOperatorProcessingContext, Class<?>, ParameterExpression<?>>
        parameterExpressionMappingFunction() {
      return (context, aClass) -> {
        return context.criteriaBuilder().parameter(aClass);
      };
    }

    private void addValueTypesAsParameterExpressionsToExpressionsListUsingContext(
        JpaCriteriaOperatorProcessingContext context) {
      for (int i = 0; i < valueTypes.size(); i++) {
        expressionList.add(parameterExpressionMappingFunction().apply(context, valueTypes.get(i)));
      }
    }

    private JpaExpressionOperand getExpressionOperandFromExpressionAtIndex(int idx) {
      return JpaExpressionOperandImpl.builder()
          .valueSupplier(() -> expressionList.get(idx))
          .build();
    }

    private List<JpaExpressionOperand> getExpressionsAsOperands() {
      List<JpaExpressionOperand> jpaExpressionOperands = new ArrayList<>();
      for (int i = 0; i < expressionList.size(); i++) {
        JpaExpressionOperand jpaExpressionOperand = getExpressionOperandFromExpressionAtIndex(i);
        jpaExpressionOperands.add(jpaExpressionOperand);
      }
      return jpaExpressionOperands;
    }
  }
}
