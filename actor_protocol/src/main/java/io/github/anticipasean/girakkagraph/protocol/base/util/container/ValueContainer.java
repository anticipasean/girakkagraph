package io.github.anticipasean.girakkagraph.protocol.base.util.container;

import akka.actor.typed.ActorRef;
import akka.actor.typed.receptionist.ServiceKey;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import javax.persistence.criteria.Expression;

public interface ValueContainer<T, C, V> {

  static <V> OptionalValueContainer<V> buildContainer(Optional<V> value, Class<V> valueType) {
    return OptionalValueContainerImpl.<V>builder().value(value).type(valueType).build();
  }

  static <E> ListValueContainer<E> buildContainer(List<E> value, Class<E> elementType) {
    return ListValueContainerImpl.<E>builder().value(value).type(elementType).build();
  }

  static ClassValueContainer buildContainer(Class<Object> classReference) {
    return ClassValueContainerImpl.builder().value(classReference).build();
  }

  static <R> ActorRefValueContainer<R> buildContainer(ActorRef<R> value, Class<R> replyType) {
    return ActorRefValueContainerImpl.<R>builder().value(value).type(replyType).build();
  }

  static <R> ServiceKeyValueContainer<R> buildContainer(ServiceKey<R> value, Class<R> commandType) {
    return ServiceKeyValueContainerImpl.<R>builder().value(value).type(commandType).build();
  }

  static <V> DefaultValueContainer<V> buildContainer(V value, Class<V> valueType) {
    return DefaultValueContainerImpl.<V>builder()
        .value(new AtomicReference<>(value))
        .type(valueType)
        .build();
  }

  @SuppressWarnings("unchecked")
  static <X> ExpressionValueContainer<X> buildContainer(Expression<X> value) {
    return ExpressionValueContainerImpl.<X>builder()
        .value(value)
        .type((Class<X>) value.getJavaType())
        .build();
  }

  static <E> List<E> getListValue(ListValueContainer<E> listContainer) {
    return listContainer.value();
  }

  static <R> ActorRef<R> getActorRefValue(ActorRefValueContainer<R> actorRefContainer) {
    return actorRefContainer.value();
  }

  static Class<?> getClassValue(ClassValueContainer classContainer) {
    return classContainer.value();
  }

  static <V> Optional<V> getOptionalValue(OptionalValueContainer<V> optionalValueContainer) {
    return optionalValueContainer.value();
  }

  static <V> AtomicReference<V> getAtomicRefValue(DefaultValueContainer<V> defaultValueContainer) {
    return defaultValueContainer.value();
  }

  static <X> Expression<X> getExpressionValue(
      ExpressionValueContainer<X> expressionValueContainer) {
    return expressionValueContainer.value();
  }

  Class<T> type();

  String name();

  Class<C> containerType();

  V value();

  default Type javaType() {
    return value().getClass().getGenericSuperclass();
  }

  //  default <T, C, V> List<ListValueContainer<V>> getListValueContainersFrom(
  //      List<ValueContainer<T, C, V>> valueContainers) {
  //    return filterValueContainers(
  //            valueContainers, valueContainer -> valueContainer instanceof ListValueContainer)
  //        .map(valueContainer -> (ListValueContainer<V>) valueContainer)
  //        .collect(Collectors.toList());
  //  }
  //
  //  default <T, C, V> List<DefaultValueContainer<V>> getDefaultValueContainersFrom(
  //      List<ValueContainer<T, C, V>> valueContainers) {
  //    return filterValueContainers(
  //            valueContainers, valueContainer -> valueContainer instanceof DefaultValueContainer)
  //        .map(valueContainer -> (DefaultValueContainer<V>) valueContainer)
  //        .collect(Collectors.toList());
  //  }
  //
  //  default <T, C, V> List<OptionalValueContainer<V>> getOptionalValueContainersFrom(
  //      List<ValueContainer<T, C, V>> valueContainers) {
  //    return filterValueContainers(
  //            valueContainers, valueContainer -> valueContainer instanceof OptionalValueContainer)
  //        .map(valueContainer -> (OptionalValueContainer<V>) valueContainer)
  //        .collect(Collectors.toList());
  //  }
  //
  //  @SuppressWarnings("unchecked")
  //  default <T, C, V> List<ActorRefValueContainer<T>> getActorRefValueContainersFrom(
  //      List<ValueContainer<T, C, V>> valueContainers) {
  //    return filterValueContainers(
  //            valueContainers, valueContainer -> valueContainer instanceof
  // ActorRefValueContainer<?>)
  //        .map(valueContainer -> (ActorRefValueContainer<T>) valueContainer)
  //        .collect(Collectors.toList());
  //  }
  //
  //  default <T, C, V> Stream<ValueContainer<T, C, V>> filterValueContainers(
  //      List<ValueContainer<T, C, V>> valueContainers, Predicate<ValueContainer<T, C, V>>
  // predicate) {
  //    return valueContainers.stream().flatMap(Stream::of).filter(predicate::test);
  //  }
  //
  //  default <V> V getDefaultContainerValue(DefaultValueContainer<V> defaultValueContainer) {
  //    AtomicReference<V> atomicRefValueHolder = getAtomicRefValue(defaultValueContainer);
  //    if (defaultValueContainer.type().isInstance(atomicRefValueHolder.get())) {
  //      return defaultValueContainer.type().cast(defaultValueContainer.value());
  //    } else {
  //      throw new ClassCastException(
  //          "The value placed in the default value container for [ "
  //              + Stream.of(
  //                      Pair.create("type", defaultValueContainer.type().getName()),
  //                      Pair.create("name", defaultValueContainer.name()))
  //                  .map(pair -> pair.first() + ": " + pair.second())
  //                  .collect(Collectors.joining(", "))
  //              + " ] does not cast to the expected type: [ holder_value: "
  //              + atomicRefValueHolder.get()
  //              + " ]");
  //    }
  //  }
  //  @SuppressWarnings("unchecked")
  //  default <R> Optional<ActorRef<R>> getActorRefValueIfForCommandType(
  //      ActorRefValueContainer<?> actorRefValueContainer, Class<R> expectedCommandType) {
  //    Optional<ActorRefValueContainer<?>> refValueContainerMaybe =
  // Optional.ofNullable(actorRefValueContainer);
  //    if (refValueContainerMaybe.isPresent() &&
  // refValueContainerMaybe.get().type().equals(expectedCommandType)) {
  //      ActorRef<R> actorRef = (ActorRef<R>) refValueContainerMaybe.get().value();
  //      return Optional.of(actorRef);
  //    } else {
  //      return Optional.empty();
  //    }
  //  }

}
