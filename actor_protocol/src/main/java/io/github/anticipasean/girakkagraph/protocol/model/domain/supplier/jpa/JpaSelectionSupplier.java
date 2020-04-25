package io.github.anticipasean.girakkagraph.protocol.model.domain.supplier.jpa;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Selection;

public interface JpaSelectionSupplier extends CriterionSupplier<Selection<?>> {

  @SuppressWarnings("unchecked")
  default <T> Selection<T> selection() {
    return (Selection<T>) get();
  }

  @SuppressWarnings("unchecked")
  default <T> Optional<Path<T>> asPathIfApt() {
    return Optional.of(selection()).filter(sel -> sel instanceof Path).map(sel -> (Path<T>) sel);
  }

  @SuppressWarnings("unchecked")
  default <C> Optional<Expression<C>> asCollectionExpressionIfApt() {
    return Optional.of(selection())
        .filter(sel -> Collection.class.isAssignableFrom(sel.getJavaType()))
        .map(sel -> (Expression<C>) sel);
  }

  @SuppressWarnings("unchecked")
  default <M extends Map<K, V>, K, V> Optional<Expression<M>> asMapExpressionIfApt() {
    return Optional.of(selection())
        .filter(sel -> sel instanceof Expression)
        .map(sel -> (Expression<?>) sel)
        .filter(expression -> Map.class.isAssignableFrom(expression.getJavaType()))
        .map(sel -> (Expression<M>) sel);
  }
}
