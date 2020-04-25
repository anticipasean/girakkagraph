package io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa;

import java.util.Optional;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

public interface JoinExpressionSupplier extends FromObjectSupplier {
  @SuppressWarnings("unchecked")
  default <R, J> Optional<SingularAttribute<R, J>> singularAttributeIfApt() {
    return Optional.of(joinObject().getAttribute())
        .filter(attribute -> attribute instanceof SingularAttribute)
        .map(attribute -> (SingularAttribute) attribute);
  }

  @SuppressWarnings("unchecked")
  default <R, C, J> Optional<PluralAttribute<R, C, J>> pluralAttributeIfApt() {
    return Optional.of(joinObject().getAttribute())
        .filter(attribute -> attribute instanceof PluralAttribute)
        .map(attribute -> (PluralAttribute) attribute);
  }

  @SuppressWarnings("unchecked")
  default <R, J> Join<R, J> joinObject() {
    return (Join<R, J>) get();
  }

  @Override
  default <R, J> From<R, J> fromObject() {
    return joinObject();
  }
}
