package io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa;

import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;

public interface JpaEntityTypeSupplier extends ManagedTypeSupplier<EntityType<?>> {

  default Bindable.BindableType bindableType() {
    return jpaEntityType().getBindableType();
  }

  default String jpaEntityName() {
    return jpaEntityType().getName();
  }

  @SuppressWarnings("unchecked")
  default <X> EntityType<X> jpaEntityType() {
    return (EntityType<X>) get();
  }

  @Override
  default <X> ManagedType<X> managedType() {
    return jpaEntityType();
  }
}
