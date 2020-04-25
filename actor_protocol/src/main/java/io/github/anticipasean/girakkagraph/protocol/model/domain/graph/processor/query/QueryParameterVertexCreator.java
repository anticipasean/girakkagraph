package io.github.anticipasean.girakkagraph.protocol.model.domain.graph.processor.query;

import io.github.anticipasean.girakkagraph.protocol.model.domain.argument.jpa.CriteriaJpaFunctionalArgument;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.JpaCriteriaArgumentEdge;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.vertex.FunctionalParameterVertexImpl;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.vertex.ParameterVertex;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.vertex.RepresentativeParameterVertexImpl;
import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.FunctionalCriterionSupplier;
import io.github.anticipasean.girakkagraph.typematcher.TypeMatcher;
import java.util.function.Function;
import java.util.function.Supplier;

interface QueryParameterVertexCreator
    extends Function<JpaCriteriaArgumentEdge, ParameterVertex> {

  static QueryParameterVertexCreator newInstance() {
    return new QueryParameterVertexCreator() {
      @Override
      public ParameterVertex apply(JpaCriteriaArgumentEdge criteriaJpaArgumentEdge) {
        return QueryParameterVertexCreator.super.apply(criteriaJpaArgumentEdge);
      }
    };
  }

  @Override
  default ParameterVertex apply(JpaCriteriaArgumentEdge criteriaJpaArgumentEdge) {
    return TypeMatcher.whenTypeOf(criteriaJpaArgumentEdge)
        .is(
            JpaCriteriaArgumentEdge.class,
            jpaCriteriaArgumentEdge ->
                jpaCriteriaArgumentEdge.modelArgument() instanceof CriteriaJpaFunctionalArgument)
        .thenApply(functionalParameterVertexCreator())
        .is(JpaCriteriaArgumentEdge.class)
        .thenApply(representativeParameterVertexCreator())
        .orElseThrow(
            criteriaJpaArgumentEdgeTypeNotMappedExceptionSupplier(criteriaJpaArgumentEdge));
  }

  default Supplier<IllegalStateException> criteriaJpaArgumentEdgeTypeNotMappedExceptionSupplier(
      JpaCriteriaArgumentEdge criteriaJpaArgumentEdge) {
    Function<JpaCriteriaArgumentEdge, String> messageGenerator =
        jpaCriteriaArgumentEdge ->
            String.format(
                "unable to map the given jpa criteria argument edge [ %s ] to a parameter vertex; this type appears to be unhandled",
                jpaCriteriaArgumentEdge);
    return () -> new IllegalStateException(messageGenerator.apply(criteriaJpaArgumentEdge));
  }

  default Function<JpaCriteriaArgumentEdge, ParameterVertex> functionalParameterVertexCreator() {
    return jpaCriteriaArgumentEdge -> {
      if (jpaCriteriaArgumentEdge.modelArgument().operationResult()
          instanceof FunctionalCriterionSupplier) {
        return FunctionalParameterVertexImpl.builder()
            .vertexPath(jpaCriteriaArgumentEdge.childPath())
            .functionalParameterType(
                ((FunctionalCriterionSupplier)
                        jpaCriteriaArgumentEdge.modelArgument().operationResult())
                    .inputType())
            .build();
      }
      Supplier<String> messageSupplier =
          () ->
              String.format(
                  "unable to retrieve the functional input type for "
                      + "jpa criteria argument edge [ %s ] because its operation result does"
                      + " map to a FunctionalCriterionSupplier",
                  jpaCriteriaArgumentEdge);
      throw new IllegalStateException(messageSupplier.get());
    };
  }

  default Function<JpaCriteriaArgumentEdge, ParameterVertex>
      representativeParameterVertexCreator() {
    return jpaCriteriaArgumentEdge -> {
      if (jpaCriteriaArgumentEdge.modelArgument().operandSet().operands().size() >= 1) {
        return RepresentativeParameterVertexImpl.builder()
            .vertexPath(jpaCriteriaArgumentEdge.childPath())
            .representativeType(
                jpaCriteriaArgumentEdge
                    .modelArgument()
                    .operandSet()
                    .operands()
                    .get(0)
                    .representedType())
            .build();
      }
      Supplier<String> messageSupplier =
          () ->
              String.format(
                  "unable to retrieve the representative type for "
                      + "jpa criteria argument edge [ %s ] because its operand set "
                      + "does not have any operands",
                  jpaCriteriaArgumentEdge);
      throw new IllegalStateException(messageSupplier.get());
    };
  }
}
