package io.github.anticipasean.girakkagraph.modifact.generation.javasource.naming;

import java.util.function.UnaryOperator;
import org.hibernate.mapping.Table;

public interface JavaSourceEntityNamingConvention {

  default UnaryOperator<String> tableNameNormalizer() {
    return s -> s;
  }

  default UnaryOperator<String> normalizedTableNameToBaseEntityTypeNameMapper() {
    return s -> s;
  }

  default UnaryOperator<String> entityNameToModifiableJpaFormatEntityClassNameMapper() {
    return s -> s;
  }

  default UnaryOperator<String> entityNameToImmutableEntityImplementationClassNameMapper() {
    return s -> s;
  }

  default EntityTypeName createEntityTypeNameFromTable(Table table) {
    String entityTypeName =
        tableNameNormalizer().andThen(normalizedTableNameToBaseEntityTypeNameMapper()).apply(table.getName());
    return EntityTypeNameImpl.builder()
        .tableName(table.getName())
        .normalizedEntityName(entityTypeName)
        .modifiableJpaFormatEntityClassName(
            entityNameToModifiableJpaFormatEntityClassNameMapper().apply(entityTypeName))
        .immutableEntityImplementationClassName(
            entityNameToImmutableEntityImplementationClassNameMapper().apply(entityTypeName))
        .build();
  }
}
