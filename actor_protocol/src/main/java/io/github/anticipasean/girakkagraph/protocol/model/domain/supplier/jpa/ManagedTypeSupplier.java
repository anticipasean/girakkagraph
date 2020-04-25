package io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

public interface ManagedTypeSupplier<M extends ManagedType<?>> extends CriterionSupplier<M> {

  @SuppressWarnings("unchecked")
  default <X> ManagedType<X> managedType() {
    return (ManagedType<X>) get();
  }

  default <X> Set<Attribute<? super X, ?>> jpaAttributes() {
    ManagedType<X> managedType = managedType();
    return managedType.getAttributes();
  }

  default <T> Optional<Attribute<? super T, ?>> attributeOneOrNoneMatching(
      Predicate<Attribute<? super T, ?>> predicate) {
    Set<Attribute<? super T, ?>> attributes = jpaAttributes();
    return attributes.stream().filter(Objects.requireNonNull(predicate, "predicate")).findAny();
  }

  default <T> Optional<SingularAttribute<? super T, ?>> singularAttributeOneOrNoneMatching(
      Predicate<SingularAttribute<? super T, ?>> predicate) {
    ManagedType<T> managedType = managedType();
    return managedType.getSingularAttributes().stream()
        .filter(Objects.requireNonNull(predicate, "predicate"))
        .findAny();
  }

  default <T> Optional<ListAttribute<? super T, ?>> listAttributeOneOrNoneMatching(
      Predicate<ListAttribute<? super T, ?>> predicate) {
    ManagedType<T> managedType = managedType();
    return managedType.getPluralAttributes().stream()
        .filter(
            pluralAttribute ->
                pluralAttribute.getCollectionType().equals(PluralAttribute.CollectionType.LIST))
        .map(Attribute::getName)
        .<ListAttribute<? super T, ?>>map(managedType::getList)
        .filter(Objects.requireNonNull(predicate, "predicate"))
        .findAny();
  }

  default <T> Optional<SetAttribute<? super T, ?>> setAttributeOneOrNoneMatching(
      Predicate<SetAttribute<? super T, ?>> predicate) {
    ManagedType<T> managedType = managedType();
    return managedType.getPluralAttributes().stream()
        .filter(
            pluralAttribute ->
                pluralAttribute.getCollectionType().equals(PluralAttribute.CollectionType.SET))
        .map(Attribute::getName)
        .<SetAttribute<? super T, ?>>map(managedType::getSet)
        .filter(Objects.requireNonNull(predicate, "predicate"))
        .findAny();
  }
}
