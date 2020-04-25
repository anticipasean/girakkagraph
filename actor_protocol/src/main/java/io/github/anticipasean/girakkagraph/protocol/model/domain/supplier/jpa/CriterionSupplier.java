package io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa;

import io.github.anticipasean.girakkagraph.protocol.base.util.valuesupplier.ValueSupplier;

/**
 * Marker interface for sub-interfaces and implementations the purpose of which is to hold a value
 * for the Java Persistance or Criteria API e.g. javax.persistence.metamodel.Attribute Since many of
 * these values are generic and involve multiple type parameters e.g. PluralAttribute<X, C, E> it is
 * easier to use a holder instance for making typed references in code when the wildcard references
 * would involve lots of casting and "unchecked" warnings all over the place
 */
public interface CriterionSupplier<V> extends ValueSupplier<V> {}
