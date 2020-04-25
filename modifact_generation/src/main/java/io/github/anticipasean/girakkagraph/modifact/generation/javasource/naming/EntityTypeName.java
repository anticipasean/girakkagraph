package io.github.anticipasean.girakkagraph.modifact.generation.javasource.naming;

import org.immutables.value.Value;

@Value.Immutable
public interface EntityTypeName {

  String tableName();

  String packageName();

  String normalizedEntityName();

  String modifiableJpaFormatEntityClassName();

  String immutableEntityImplementationClassName();

  @Value.Derived
  default String fullEntityClassName() {
    return new StringBuilder(packageName()).append(normalizedEntityName()).toString();
  }

  @Value.Derived
  default String fullModifiableJpaEntityClassName() {
    return new StringBuilder(packageName()).append(modifiableJpaFormatEntityClassName()).toString();
  }

  @Value.Derived
  default String fullImmutableEntityImplementationClassName() {
    return new StringBuilder(packageName())
        .append(immutableEntityImplementationClassName())
        .toString();
  }
}
