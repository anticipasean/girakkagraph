package io.github.anticipasean.girakkagraph.protocol.model.domain.index.type;

import io.github.anticipasean.girakkagraph.protocol.model.domain.index.ModelType;
import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.JpaEmbeddableSupplier;
import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.JpaEntityTypeSupplier;
import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.ManagedTypeSupplier;
import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.SingularJpaAttributeSupplier;
import java.util.Optional;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Type;
import org.immutables.value.Value;

interface PersistableType extends ModelType {

  ManagedType<?> jpaManagedType();

  @Value.Derived
  default ManagedTypeSupplier<?> jpaManagedTypeSupplier() {
    return jpaEntitySupplierIfApt().isPresent()
        ? jpaEntitySupplierIfApt().get()
        : jpaEmbeddableSupplierIfApt().isPresent()
            ? jpaEmbeddableSupplierIfApt().get()
            : this::jpaManagedType;
  }

  @Value.Derived
  default Type.PersistenceType persistenceType() {
    return jpaManagedType().getPersistenceType();
  }

  @Value.Derived
  default Class<?> javaType() {
    return jpaManagedType().getJavaType();
  }

  @Value.Derived
  default Optional<JpaEntityTypeSupplier> jpaEntitySupplierIfApt() {
    return Optional.of(jpaManagedType())
        .filter(managedType -> managedType instanceof EntityType)
        .map(managedType -> (EntityType<?>) managedType)
        .map(entityType -> () -> entityType);
  }

  @Value.Derived
  default Optional<JpaEmbeddableSupplier> jpaEmbeddableSupplierIfApt() {
    return Optional.of(jpaManagedType())
        .filter(managedType -> managedType instanceof EmbeddableType)
        .map(managedType -> (EmbeddableType<?>) managedType)
        .map(embeddableType -> () -> embeddableType);
  }

  @Value.Derived
  default Optional<SingularJpaAttributeSupplier> singularIdAttributeSupplierIfApt() {
    return jpaEntitySupplierIfApt()
        .map(JpaEntityTypeSupplier::jpaEntityType)
        .filter(IdentifiableType::hasSingleIdAttribute)
        .map(entityType -> entityType.getId(entityType.getIdType().getJavaType()))
        .map(singularAttribute -> () -> singularAttribute);
  }

  @Value.Derived
  default boolean isEmbeddable() {
    return persistenceType().equals(Type.PersistenceType.EMBEDDABLE);
  }

  @Value.Derived
  default boolean isEntity() {
    return persistenceType().equals(Type.PersistenceType.ENTITY);
  }
}
