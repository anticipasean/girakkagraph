package io.github.anticipasean.girakkagraph.modifact.generation.javasource.naming.impl;

import cyclops.control.Option;
import cyclops.data.HashSet;
import io.github.anticipasean.girakkagraph.modifact.generation.javasource.naming.JavaSourceEntityNamingConvention;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public enum DefaultJavaSourceEntityNamingConvention implements JavaSourceEntityNamingConvention {
  INSTANCE;

  public static JavaSourceEntityNamingConvention getInstance(){
    return INSTANCE;
  }

  private static final HashSet<String> RESERVED_KEYWORDS =
      HashSet.of(
          "abstract",
          "assert",
          "boolean",
          "break",
          "byte",
          "case",
          "catch",
          "char",
          "class",
          "const",
          "continue",
          "default",
          "do",
          "double",
          "else",
          "enum",
          "extends",
          "final",
          "finally",
          "float",
          "for",
          "goto",
          "if",
          "implements",
          "import",
          "instanceof",
          "int",
          "interface",
          "long",
          "native",
          "new",
          "package",
          "private",
          "protected",
          "public",
          "return",
          "short",
          "static",
          "strictfp",
          "super",
          "switch",
          "synchronized",
          "this",
          "throw",
          "throws",
          "transient",
          "try",
          "void",
          "volatile",
          "while");

  private static boolean isReservedJavaKeyword(String str) {
    return RESERVED_KEYWORDS.contains(str);
  }

  @Override
  public UnaryOperator<String> tableNameNormalizer() {
    return tableName -> tableName;
  }

  @Override
  public UnaryOperator<String> normalizedTableNameToBaseEntityTypeNameMapper() {
    return normalizedTableName -> {
      if (isReservedJavaKeyword(normalizedTableName.toLowerCase())) {
        Supplier<String> messageSupplier =
            () ->
                String.format(
                    "an entity type name may not be a reserved java keyword: %s",
                    normalizedTableName);
        throw new IllegalArgumentException(messageSupplier.get());
      }
      return Option.some(normalizedTableName)
          .filter(s -> s.length() > 1 && Character.isLowerCase(s.charAt(0)))
          .map(
              s ->
                  new StringBuilder(Character.toUpperCase(s.charAt(0)))
                      .append(s.substring(1))
                      .toString())
          .orElse(normalizedTableName);
    };
  }

  @Override
  public UnaryOperator<String> entityNameToModifiableJpaFormatEntityClassNameMapper() {
    return entityName -> new StringBuilder(entityName).append("Jpa").toString();
  }

  @Override
  public UnaryOperator<String> entityNameToImmutableEntityImplementationClassNameMapper() {
    return entityName -> new StringBuilder(entityName).append("Impl").toString();
  }
}
