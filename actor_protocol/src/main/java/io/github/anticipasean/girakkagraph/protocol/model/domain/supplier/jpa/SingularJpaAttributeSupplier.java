package io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.SingularAttribute;

public interface SingularJpaAttributeSupplier
    extends JpaAttributeSupplier<SingularAttribute<?, ?>> {

  /**
   * This value cannot be included as a derived value in in the immutable subtypes created because
   * the immutables generator at this time does not handle generic methods but it is not desirable
   * based on the usage of this class to make this class generic
   *
   * @param <X> entity java class to which this jpa attribute maps
   * @param <T> basic (non-collection) type of the attribute in question
   * @return singular jpa attribute
   */
  @SuppressWarnings("unchecked")
  default <X, T> SingularAttribute<X, T> singularAttribute() {
    return (SingularAttribute<X, T>) get();
  }

  @Override
  default <X, T> Attribute<X, T> jpaAttribute() {
    return singularAttribute();
  }
}
