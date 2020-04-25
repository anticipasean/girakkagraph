package io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SetAttribute;

public interface PluralJpaAttributeSupplier extends JpaAttributeSupplier<PluralAttribute<?, ?, ?>> {
  @SuppressWarnings("unchecked")
  default <X, C extends List<E>, E> Optional<ListAttribute<X, E>> asListAttribute() {
    PluralAttribute<X, C, E> pluralAttribute = pluralAttribute();
    if (pluralAttribute instanceof ListAttribute) {
      return Optional.of((ListAttribute<X, E>) pluralAttribute);
    }
    return Optional.empty();
  }

  @SuppressWarnings("unchecked")
  default <X, C extends Set<E>, E> Optional<SetAttribute<X, E>> asSetAttribute() {
    PluralAttribute<X, C, E> pluralAttribute = pluralAttribute();
    if (pluralAttribute instanceof SetAttribute) {
      return Optional.of((SetAttribute<X, E>) pluralAttribute);
    }
    return Optional.empty();
  }

  @SuppressWarnings("unchecked")
  default <X, C extends Map<K, V>, K, V> Optional<MapAttribute<X, K, V>> asMapAttribute() {
    PluralAttribute<X, C, K> pluralAttribute = pluralAttribute();
    if (pluralAttribute instanceof MapAttribute) {
      return Optional.of((MapAttribute<X, K, V>) pluralAttribute);
    }
    return Optional.empty();
  }

  @SuppressWarnings("unchecked")
  default <X, C extends Collection<E>, E>
      Optional<CollectionAttribute<X, E>> asCollectionAttribute() {
    PluralAttribute<X, C, E> pluralAttribute = pluralAttribute();
    if (pluralAttribute instanceof CollectionAttribute) {
      return Optional.of((CollectionAttribute<X, E>) pluralAttribute);
    }
    return Optional.empty();
  }

  @Override
  default <X, C> Attribute<X, C> jpaAttribute() {
    return pluralAttribute();
  }

  /**
   * This value cannot be included as a derived value in in the immutable subtypes created because
   * the immutables generator at this time does not handle generic methods but it is not desirable
   * based on the usage of this class to make this class generic
   *
   * @param <X> entity java class to which this jpa attribute maps
   * @param <C> the container type: list, set, map, etc.
   * @param <E> the element type of that container type
   * @return the jpa attribute cast as plural jpa attribute or null if not applicable
   */
  @SuppressWarnings("unchecked")
  default <X, C, E> PluralAttribute<X, C, E> pluralAttribute() {
    return (PluralAttribute<X, C, E>) get();
  }
}
