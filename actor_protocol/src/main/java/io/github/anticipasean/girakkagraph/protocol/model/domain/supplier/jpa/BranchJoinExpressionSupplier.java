package io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa;

import java.util.Optional;
import javax.persistence.criteria.Join;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

public interface BranchJoinExpressionSupplier extends JoinExpressionSupplier {

  @SuppressWarnings("unchecked")
  default <X, P> Optional<SingularAttribute<X, P>> parentSingularAttributeIfApt() {
    Join<X, P> parentJoin = parentJoin();
    return Optional.of(parentJoin)
        .map(Join::getAttribute)
        .filter(attribute -> attribute instanceof SingularAttribute)
        .map(attribute -> (SingularAttribute) attribute);
  }

  @SuppressWarnings("unchecked")
  default <X, P> Join<X, P> parentJoin() {
    return Optional.of(joinObject().getParent())
        .filter(pFrom -> pFrom instanceof Join)
        .map(pFrom -> (Join<X, P>) pFrom)
        .orElseThrow(() -> new IllegalArgumentException("branch join parent is not a join"));
  }

  @SuppressWarnings("unchecked")
  default <X, L, P> Optional<PluralAttribute<X, L, P>> parentPluralAttributeIfApt() {
    Join<X, P> parentJoin = parentJoin();
    return Optional.of(parentJoin)
        .map(Join::getAttribute)
        .filter(attribute -> attribute instanceof PluralAttribute)
        .map(attribute -> (PluralAttribute) attribute);
  }
}
