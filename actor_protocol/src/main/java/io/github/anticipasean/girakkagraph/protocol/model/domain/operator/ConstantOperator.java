package io.github.anticipasean.girakkagraph.protocol.model.domain.operator;

public interface ConstantOperator {
  // extends Operator<NullaryOperandSet, Object> {
  //  enum Nullary implements ConstantOperator {
  //    CONSTANT;
  //
  //    @Override
  //    public String callName() {
  //      return "constant";
  //    }
  //
  //    @Override
  //    public Predicate<NullaryOperandSet> operandSetValidityTest() {
  //      return nullaryOperandSet -> true;
  //    }
  //
  //    @Override
  //    public Operation<NullaryOperandSet, Object> operation() {
  //      return (nullaryOperandSet) -> {
  //        return null;
  //      };
  //    }
  //
  //    @Override
  //    public Arity arity() {
  //      return Arity.NULLARY;
  //    }
  //
  //    @Override
  //    public NullReturnValue executeOperationOnOperandSetInProcessingContext(
  //        NullaryOperandSet<Operand> operandSet, ProcessingContext processingContext) {
  //      return NullReturnValue.INSTANCE;
  //    }
  //
  //    @Override
  //    public Arity arity() {
  //      return Arity.NULLARY;
  //    }

  //    @Override
  //    public Class<? extends ReturnValue> returnValueType() {
  //      return NullReturnValue.class;
  //    }
  //
  //    @Override
  //    public List<Class<? extends Operand>> expectedOperandTypes() {
  //      return Collections.emptyList();
  //    }
  //
  //    @Override
  //    public BiFunction<CriteriaBuilder, List<Operand>, ReturnValue>
  //        criteriaOperatorMappingFunction() {
  //      return (criteriaBuilder, operandList) -> {
  //        return NullReturnValue.INSTANCE;
  //      };
  //    }

  //  }
}
