package io.github.anticipasean.girakkagraph.protocol.graphql.domain.scalar;

import akka.japi.Pair;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import graphql.Scalars;
import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLScalarType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface GraphQLScalarsSupport {

  static CoercibleJavaTypeGraphQLScalarMapper getMapperInstance() {
    return GraphQLScalarTypeMaps.INSTANCE.coercibleJavaTypeGraphQLScalarMapper();
  }

  static Map<String, GraphQLScalarType> extendedScalarsMap() {
    return GraphQLScalarTypeMaps.INSTANCE.extendedScalars();
  }

  static Map<String, GraphQLScalarType> allScalarsMap() {
    return GraphQLScalarTypeMaps.INSTANCE.allScalars();
  }

  static Map<String, Set<Class<?>>> graphQLScalarTypeNameToCoercibleJavaTypeSetMap() {
    return GraphQLScalarTypeMaps.INSTANCE.graphQLScalarTypeNameToCoercibleJavaTypeSetMap();
  }

  static Map<Class<?>, GraphQLScalarType> coercibleJavaTypeToGraphQLScalarTypeMap() {
    return GraphQLScalarTypeMaps.INSTANCE.coercibleJavaTypeToGraphQLScalarTypeMap();
  }

  /**
   * GraphQLScalarTypes should not be used as keys since they are not immutable and could lead to a
   * situation where two or more of the same type occupy the different slots within the map
   * Restricting the creation of the scalar types to be used in the application to just those
   * provided by this enum should cut down on some of the troubles that come from type objects not
   * being immutable though not completely since there is not a way at this time to subclass or
   * overwrite type GraphQLScalarType
   *
   * Use of this singleton enum instance ensures these maps are only generated once and
   * the graphql scalar types are not being regenerated elsewhere
   */
  static enum GraphQLScalarTypeMaps {
    INSTANCE(
        Stream.concat(
                basicScalarTypeCoercedJavaTypePairStream(),
                extendedScalarTypeCoercedJavaTypePairStream())
            .reduce(
                ImmutableMap.<Class<?>, GraphQLScalarType>builder(),
                (classGraphQLScalarTypeBuilder, classGraphQLScalarTypePair) ->
                    classGraphQLScalarTypeBuilder.put(
                        classGraphQLScalarTypePair.first(), classGraphQLScalarTypePair.second()),
                (classGraphQLScalarTypeBuilder, classGraphQLScalarTypeBuilder2) ->
                    classGraphQLScalarTypeBuilder2)
            .build());
    private final Map<Class<?>, GraphQLScalarType> coercibleJavaTypeToGraphQLScalarMap;
    private final Map<String, GraphQLScalarType> extendedScalars;
    private final Map<String, GraphQLScalarType> allScalars;
    private final Map<String, Set<Class<?>>> graphQLScalarTypeNameToCoercibleJavaTypeSetMap;
    private final CoercibleJavaTypeGraphQLScalarMapper coercibleJavaTypeGraphQLScalarMapper;

    GraphQLScalarTypeMaps(Map<Class<?>, GraphQLScalarType> coercibleJavaTypeToGraphQLScalarMap) {
      this.coercibleJavaTypeToGraphQLScalarMap = coercibleJavaTypeToGraphQLScalarMap;
      this.coercibleJavaTypeGraphQLScalarMapper =
          new CoercibleJavaTypeGraphQLScalarMapper(coercibleJavaTypeToGraphQLScalarMap);
      Set<String> extendedScalarNames =
          extendedScalarTypeCoercedJavaTypePairStream()
              .map(Pair::second)
              .map(GraphQLScalarType::getName)
              .collect(Collectors.toSet());
      this.extendedScalars =
          generateImmutableMapOfExtendedScalars(
              coercibleJavaTypeToGraphQLScalarMap, extendedScalarNames);
      this.allScalars = generateImmutableMapOfAllScalars(coercibleJavaTypeToGraphQLScalarMap);
      this.graphQLScalarTypeNameToCoercibleJavaTypeSetMap =
          generateImmutableCoercibleJavaTypesToGraphQLScalarTypes(
              coercibleJavaTypeToGraphQLScalarMap);
    }

    private static Stream<Pair<Class<?>, GraphQLScalarType>>
        basicScalarTypeCoercedJavaTypePairStream() {
      return Stream.of(
          Pair.create(Integer.class, Scalars.GraphQLInt),
          Pair.create(int.class, Scalars.GraphQLInt),
          Pair.create(Float.class, Scalars.GraphQLFloat),
          Pair.create(float.class, Scalars.GraphQLFloat),
          Pair.create(String.class, Scalars.GraphQLString),
          Pair.create(Boolean.class, Scalars.GraphQLBoolean),
          Pair.create(boolean.class, Scalars.GraphQLBoolean),
          Pair.create(Long.class, Scalars.GraphQLLong),
          Pair.create(long.class, Scalars.GraphQLLong),
          Pair.create(Short.class, Scalars.GraphQLShort),
          Pair.create(short.class, Scalars.GraphQLShort),
          Pair.create(Byte.class, Scalars.GraphQLByte),
          Pair.create(byte.class, Scalars.GraphQLByte),
          Pair.create(BigInteger.class, Scalars.GraphQLBigInteger),
          Pair.create(BigDecimal.class, Scalars.GraphQLBigDecimal),
          Pair.create(Character.class, Scalars.GraphQLChar),
          Pair.create(char.class, Scalars.GraphQLChar));
    }

    /**
     * These streams should not be called more than once to ensure the object references--hashcodes--
     * don't change since graphql-java does some type checking based on object reference rather than
     * equality tests
     * @return
     */
    private static Stream<Pair<Class<?>, GraphQLScalarType>>
        extendedScalarTypeCoercedJavaTypePairStream() {
      return Stream.of(
              Pair.create(OffsetDateTime.class, ExtendedScalars.DateTime),
              Pair.create(LocalDate.class, ExtendedScalars.Date),
              Pair.create(OffsetTime.class, ExtendedScalars.Time),
              Pair.create(Locale.class, ExtendedScalars.Locale),
              Pair.create(byte[].class, CustomScalars.Blob))
          .map(
              typeToGraphQLScalarPair ->
                  Pair.create(
                      typeToGraphQLScalarPair.first(),
                      typeToGraphQLScalarPair
                          .second()
                          .transform(
                              builder ->
                                  builder.name(
                                      Optional.of(typeToGraphQLScalarPair.first().getSimpleName())
                                          .filter(name -> name.matches("[_A-Za-z][_0-9A-Za-z]*"))
                                          .orElse(typeToGraphQLScalarPair.second().getName())))));
    }

    private ImmutableMap<String, Set<Class<?>>>
        generateImmutableCoercibleJavaTypesToGraphQLScalarTypes(
            Map<Class<?>, GraphQLScalarType> coercibleJavaTypeToGraphQLScalarMap) {
      return coercibleJavaTypeToGraphQLScalarMap.entrySet().stream()
          .map(
              classGraphQLScalarTypeEntry ->
                  Pair.create(
                      classGraphQLScalarTypeEntry.getValue().getName(),
                      classGraphQLScalarTypeEntry.getKey()))
          .collect(
              Collectors.groupingBy(
                  Pair::first,
                  ConcurrentHashMap::new,
                  Collectors.collectingAndThen(
                      Collectors.toList(),
                      pairs -> pairs.stream().map(Pair::second).collect(Collectors.toSet()))))
          .entrySet()
          .stream()
          .reduce(
              ImmutableMap.<String, Set<Class<?>>>builder(),
              (stringSetBuilder, stringSetEntry) ->
                  stringSetBuilder.put(
                      stringSetEntry.getKey(),
                      ImmutableSet.<Class<?>>builder().addAll(stringSetEntry.getValue()).build()),
              (stringSetBuilder, stringSetBuilder2) -> stringSetBuilder2)
          .build();
    }

    private ImmutableMap<String, GraphQLScalarType> generateImmutableMapOfAllScalars(
        Map<Class<?>, GraphQLScalarType> coercibleJavaTypeToGraphQLScalarMap) {
      return coercibleJavaTypeToGraphQLScalarMap.values().stream()
          .distinct()
          .reduce(
              ImmutableMap.<String, GraphQLScalarType>builder(),
              (stringGraphQLScalarTypeBuilder, graphQLScalarType) ->
                  stringGraphQLScalarTypeBuilder.put(
                      graphQLScalarType.getName(), graphQLScalarType),
              (stringGraphQLScalarTypeBuilder, stringGraphQLScalarTypeBuilder2) ->
                  stringGraphQLScalarTypeBuilder2)
          .build();
    }

    private ImmutableMap<String, GraphQLScalarType> generateImmutableMapOfExtendedScalars(
        Map<Class<?>, GraphQLScalarType> coercibleJavaTypeToGraphQLScalarMap,
        Set<String> extendedScalarNames) {
      return coercibleJavaTypeToGraphQLScalarMap.values().stream()
          .filter(graphQLScalarType -> extendedScalarNames.contains(graphQLScalarType.getName()))
          .map(graphQLScalarType -> Pair.create(graphQLScalarType.getName(), graphQLScalarType))
          .reduce(
              ImmutableMap.<String, GraphQLScalarType>builder(),
              (stringGraphQLScalarTypeBuilder, stringGraphQLScalarTypePair) ->
                  stringGraphQLScalarTypeBuilder.put(
                      stringGraphQLScalarTypePair.first(), stringGraphQLScalarTypePair.second()),
              (stringGraphQLScalarTypeBuilder, stringGraphQLScalarTypeBuilder2) ->
                  stringGraphQLScalarTypeBuilder2)
          .build();
    }

    public CoercibleJavaTypeGraphQLScalarMapper coercibleJavaTypeGraphQLScalarMapper() {
      return coercibleJavaTypeGraphQLScalarMapper;
    }

    public Map<String, GraphQLScalarType> extendedScalars() {
      return extendedScalars;
    }

    public Map<String, GraphQLScalarType> allScalars() {
      return allScalars;
    }

    public Map<String, Set<Class<?>>> graphQLScalarTypeNameToCoercibleJavaTypeSetMap() {
      return graphQLScalarTypeNameToCoercibleJavaTypeSetMap;
    }

    public Map<Class<?>, GraphQLScalarType> coercibleJavaTypeToGraphQLScalarTypeMap() {
      return coercibleJavaTypeToGraphQLScalarMap;
    }
  }

  static class CoercibleJavaTypeGraphQLScalarMapper
      implements Function<Supplier<Class<?>>, Optional<GraphQLScalarType>> {

    private final Map<Class<?>, GraphQLScalarType> clsToScalarTypeMap;

    public CoercibleJavaTypeGraphQLScalarMapper(
        Map<Class<?>, GraphQLScalarType> clsToScalarTypeMap) {
      this.clsToScalarTypeMap = clsToScalarTypeMap;
    }

    @Override
    public Optional<GraphQLScalarType> apply(Supplier<Class<?>> classSupplier) {
      Objects.requireNonNull(classSupplier, "classSupplier");
      Class<?> classValue =
          Objects.requireNonNull(classSupplier.get(), "class value of classSupplier");
      if (clsToScalarTypeMap.containsKey(classValue)) {
        return Optional.of(clsToScalarTypeMap.get(classValue));
      }
      for (Class<?> mappedClassValue : clsToScalarTypeMap.keySet()) {
        if (mappedClassValue.isAssignableFrom(classValue)) {
          return Optional.of(clsToScalarTypeMap.get(classValue));
        }
      }
      return Optional.empty();
    }
  }
}
