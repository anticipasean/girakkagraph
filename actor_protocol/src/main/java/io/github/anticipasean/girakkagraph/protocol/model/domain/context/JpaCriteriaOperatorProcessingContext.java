package io.github.anticipasean.girakkagraph.protocol.model.domain.context;

import javax.persistence.criteria.CriteriaBuilder;
import org.immutables.value.Value.Immutable;

@Immutable
public interface JpaCriteriaOperatorProcessingContext extends ProcessingContext {

  CriteriaBuilder criteriaBuilder();

}
