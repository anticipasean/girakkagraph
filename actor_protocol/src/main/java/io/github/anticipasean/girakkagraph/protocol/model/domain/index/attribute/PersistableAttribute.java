package io.github.anticipasean.girakkagraph.protocol.model.domain.index.attribute;

import io.github.anticipasean.girakkagraph.protocol.model.domain.index.ModelAttribute;
import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.JpaAttributeSupplier;
import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.PluralJpaAttributeSupplier;
import io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa.SingularJpaAttributeSupplier;
import java.util.Optional;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import org.immutables.value.Value;

interface PersistableAttribute extends ModelAttribute {

  @Value.Derived
  default Class<?> singularTypeOrPluralAttributeElementType() {
    Optional<Class<?>> javaTypeIfSingular =
        singularJpaAttributeSupplierIfApt()
            .map(SingularJpaAttributeSupplier::singularAttribute)
            .map(Bindable::getBindableJavaType);
    if (javaTypeIfSingular.isPresent()) {
      return javaTypeIfSingular.get();
    }
    Optional<Class<?>> javaElementTypeIfPlural =
        pluralJpaAttributeSupplierIfApt()
            .map(PluralJpaAttributeSupplier::pluralAttribute)
            .map(Bindable::getBindableJavaType);
    return javaElementTypeIfPlural.get();
  }

  @Value.Derived
  default Optional<SingularJpaAttributeSupplier> singularJpaAttributeSupplierIfApt() {
    return Optional.of(jpaAttribute())
        .filter(jpaAttribute -> jpaAttribute instanceof SingularAttribute<?, ?>)
        .map(jpaAttribute -> () -> (SingularAttribute<?, ?>) jpaAttribute);
  }

  @Value.Derived
  default JpaAttributeSupplier<?> jpaAttributeSupplier() {
    return this::jpaAttribute;
  }

  Attribute<?, ?> jpaAttribute();

  @Value.Derived
  default Optional<PluralJpaAttributeSupplier> pluralJpaAttributeSupplierIfApt() {
    return Optional.of(jpaAttribute())
        .filter(jpaAttribute -> jpaAttribute instanceof PluralAttribute<?, ?, ?>)
        .map(jpaAttribute -> () -> (PluralAttribute<?, ?, ?>) jpaAttribute);
  }

  @Value.Derived
  default String jpaAttributeName() {
    return jpaAttribute().getName();
  }

  @Value.Derived
  default ManagedType<?> parentJpaManagedType() {
    return jpaAttribute().getDeclaringType();
  }

  @Value.Derived
  default Attribute.PersistentAttributeType persistenceAttributeType() {
    return jpaAttribute().getPersistentAttributeType();
  }

  @Value.Derived
  default boolean isSingular() {
    return !jpaAttribute().isCollection();
  }

  @Value.Derived
  default boolean isPlural() {
    return jpaAttribute().isCollection();
  }

  @Value.Derived
  default boolean isList() {
    return jpaAttribute() instanceof ListAttribute;
  }

  @Value.Derived
  default boolean isSet() {
    return jpaAttribute() instanceof SetAttribute;
  }

  @Value.Derived
  default boolean isMap() {
    return jpaAttribute() instanceof MapAttribute;
  }

  @Value.Derived
  default boolean isManyToOne() {
    return persistenceAttributeType().equals(Attribute.PersistentAttributeType.MANY_TO_ONE);
  }

  @Value.Derived
  default boolean isOneToOne() {
    return persistenceAttributeType().equals(Attribute.PersistentAttributeType.ONE_TO_ONE);
  }

  @Value.Derived
  default boolean isBasic() {
    return persistenceAttributeType().equals(Attribute.PersistentAttributeType.BASIC);
  }

  @Value.Derived
  default boolean isEmbedded() {
    return persistenceAttributeType().equals(Attribute.PersistentAttributeType.EMBEDDED);
  }

  @Value.Derived
  default boolean isManyToMany() {
    return persistenceAttributeType().equals(Attribute.PersistentAttributeType.MANY_TO_MANY);
  }

  @Value.Derived
  default boolean isOneToMany() {
    return persistenceAttributeType().equals(Attribute.PersistentAttributeType.ONE_TO_MANY);
  }

  @Value.Derived
  default boolean isElementCollection() {
    return persistenceAttributeType().equals(Attribute.PersistentAttributeType.ELEMENT_COLLECTION);
  }
}
