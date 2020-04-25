package io.github.anticipasean.girakkagraph.protocol.model.domain.operator.jpa;

import com.google.common.collect.ImmutableSet;
import io.github.anticipasean.girakkagraph.protocol.model.domain.arity.Arity;
import io.github.anticipasean.girakkagraph.protocol.model.domain.filter.PatternRecognition;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operandset.GenericRestrictions;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operandset.jpa.JpaCriteriaOperandSet;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operation.Operation;
import io.github.anticipasean.girakkagraph.protocol.model.domain.operator.Restriction;
import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.PredicateSupplier;
import java.util.Set;
import java.util.function.Predicate;
import javax.persistence.criteria.Expression;

public interface JpaStringOperator {

  Predicate<JpaCriteriaOperandSet> hasFieldAndValueStringExpressionOperands =
      jpaCriteriaOperandSet -> {
        return jpaCriteriaOperandSet.arity().operandCount() >= 1
            && String.class.isAssignableFrom(
                jpaCriteriaOperandSet.operands().get(0).valueSupplier().expression().getJavaType())
            && String.class.isAssignableFrom(
                jpaCriteriaOperandSet.operands().get(1).valueSupplier().expression().getJavaType());
      };

  enum Binary implements JpaCriteriaOperator<PredicateSupplier>, PatternRecognition {
    CONTAINS() {
      @Override
      public Set<Restriction<JpaCriteriaOperandSet>> specificRestrictions() {
        return ImmutableSet.of(GenericRestrictions.allOperandsMustRepresentTypeString());
      }

      @Override
      public Operation<JpaCriteriaOperandSet, PredicateSupplier> operation() {
        return jpaCriteriaOperandSet -> {
          Expression<Integer> locatedInStringExpression =
              jpaCriteriaOperandSet
                  .processingContext()
                  .criteriaBuilder()
                  .locate(
                      jpaCriteriaOperandSet.operands().get(0).valueSupplier().expression(),
                      jpaCriteriaOperandSet.operands().get(1).valueSupplier().expression());
          javax.persistence.criteria.Predicate predicate =
              jpaCriteriaOperandSet
                  .processingContext()
                  .criteriaBuilder()
                  .greaterThan(locatedInStringExpression, 0);
          return () -> predicate;
        };
      }
    };

    private final Arity arity;
    private final String callName;

    Binary() {
      this.arity = Arity.BINARY;
      this.callName = "contains";
    }

    @Override
    public String callName() {
      return callName;
    }

    @Override
    public Arity arity() {
      return arity;
    }
    //    private Predicate<>

  }
}
