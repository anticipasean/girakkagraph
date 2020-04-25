package io.github.anticipasean.girakkagraph.protocol.model.domain.graph.extrapolator.query;

import static io.github.anticipasean.girakkagraph.protocol.model.domain.graph.QueryModelGraph.SQL;

import akka.japi.Pair;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSortedMap;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.EdgeKeyImpl;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.ModelEdge;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.ModelEdge.EdgeKey;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.QueryModelGraph;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.FromObjectEdge;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.JoinEdge;
import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.edge.jpa.RootFromVertex;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.ModelPath;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Use for mapping model graph edges to JPA {@link javax.persistence.criteria.CriteriaQuery}
 * components
 *
 * <p>The backing map is immutable and held within an {@link AtomicReference} so that any updates
 * replace the backing map instance atomically. Also, no defensive copying of get calls is necessary
 * since the returned map is immutable.
 *
 * <p>The integrity of the map must be maintained at all times, meaning that if there is an entry
 * for ROOT, there must be corresponding entries with the same EdgeKey and ModelEdge instance in
 * JOIN and FROM since ROOTs are polymorphically of both of these types and the caller may refer to
 * just the entries under a given SQL component type for a particular search. This integrity spares
 * callers of this map from having to iterate through multiple SQL component maps to wire different
 * parts of a query, which would likely lead to a lot of code duplication elsewhere.
 *
 * <p>This integrity is maintained through atomic update calls on {@link AtomicReference} wherein
 * the new or old instances of mappings are checked for validity and corresponding entries under
 * other SQL component map values before an entire new immutable map instance is created and stored
 * in the atomic reference holder. For example, the call to remove(SQL key):
 *
 * <pre>
 *     return delegate
 *         .getAndUpdate(
 *             currentMap -> {
 *               if (key instanceof SQL && get(key) != null) {
 *                 NavigableMap{@code <EdgeKey, ModelEdge>} currentValueForKey = get(key);
 *                 return delegateMapUpdaterFunctionForAtomicUpdatesFollowingSetMembershipAndMembershipResultAction(
 *                         (SQL) key, currentValueForKey, Set::contains, Set::remove)
 *                     .apply(currentMap);
 *               }
 *               return currentMap;
 *             })
 *         .get(key);
 * </pre>
 */
public class QueryCriteriaEdgesProcessingMap implements Map<SQL, NavigableMap<EdgeKey, ModelEdge>> {
  private final AtomicReference<Map<SQL, NavigableMap<EdgeKey, ModelEdge>>> delegate;

  public QueryCriteriaEdgesProcessingMap() {
    this.delegate = new AtomicReference<>(ImmutableMap.<SQL, NavigableMap<EdgeKey, ModelEdge>>of());
  }

  @Override
  public int size() {
    return delegate.get().size();
  }

  @Override
  public boolean containsKey(Object key) {
    return delegate.get().containsKey(Objects.requireNonNull(key, "key"));
  }

  @Override
  public boolean containsValue(Object value) {
    return delegate.get().containsValue(Objects.requireNonNull(value, "value"));
  }

  @Override
  public NavigableMap<EdgeKey, ModelEdge> get(Object key) {
    return delegate.get().get(Objects.requireNonNull(key, "key"));
  }

  public Optional<ModelEdge> getLastEdgeInSequenceForSQLComponent(SQL key) {
    return Optional.ofNullable(get(key))
        .filter(modelPathModelEdgeNavigableMap -> modelPathModelEdgeNavigableMap.size() > 0)
        .map(
            modelPathModelEdgeNavigableMap ->
                modelPathModelEdgeNavigableMap.lastEntry().getValue());
  }

  public Optional<ModelEdge> getFirstEdgeInSequenceForSQLComponent(SQL key) {
    return Optional.ofNullable(get(key))
        .filter(modelPathModelEdgeNavigableMap -> modelPathModelEdgeNavigableMap.size() > 0)
        .map(
            modelPathModelEdgeNavigableMap ->
                modelPathModelEdgeNavigableMap.firstEntry().getValue());
  }

  /**
   * Selectable paths have the following restrictions:
   *
   * <ul>
   *   <li>selections cannot be {@link javax.persistence.criteria.Root} or be {@link
   *       graphql.language.Argument}s or be {@link graphql.language.Directive}s on types or
   *       attributes
   *   <li>selections cannot be made if no root has been established
   *   <li>selections cannot be made on root itself, only its attributes or descendents thereof
   * </ul>
   *
   * translation:
   *
   * <pre>
   *   (path.depth() > 1 &&
   *    path.rawArguments().isEmpty() &&
   *    path.directives().isEmpty() &&
   *    get(SQL.ROOT) != null &&
   *    get(SQL.ROOT).size() == 1)
   * </pre>
   *
   * @param path - model path of the attribute that the caller would like to create a {@link
   *     javax.persistence.criteria.Selection} or other {@link
   *     javax.persistence.criteria.Expression} on
   * @return the {@link FromObjectEdge} on which a {@link javax.persistence.criteria.Selection} can
   *     be made if there is one that fits
   */
  public Optional<FromObjectEdge> findFromObjectOnWhichPathCanBeSelectedIfPresent(ModelPath path) {
    Objects.requireNonNull(path, "path");
    if (get(SQL.ROOT) == null
        || path.depth() <= 1
        || !path.rawArguments().isEmpty()
        || !path.directives().isEmpty()
        || get(SQL.ROOT).size() == 0) {
      return Optional.empty();
    }
    ModelPath parentPath = path.parentPath();
    ModelPath grandparentPath =
        parentPath.equals(get(SQL.ROOT).firstKey().childPath())
            ? parentPath
            : parentPath.parentPath();
    ModelEdge modelEdge =
        getOrDefault(SQL.FROM, new ConcurrentSkipListMap<>(EdgeKey.comparator()))
            .get(EdgeKeyImpl.of(grandparentPath, parentPath));
    return Optional.ofNullable(modelEdge)
        .filter(edge -> edge instanceof FromObjectEdge)
        .map(edge -> (FromObjectEdge) edge);
  }

  @Override
  public NavigableMap<EdgeKey, ModelEdge> put(SQL key, NavigableMap<EdgeKey, ModelEdge> value) {
    return delegate
        .getAndUpdate(
            delegateMapUpdaterFunctionForAtomicUpdatesFollowingSetMembershipAndMembershipResultAction(
                Objects.requireNonNull(key, "key"),
                Objects.requireNonNull(value, "value"),
                (pairs, sqlModelEdgePair) -> !pairs.contains(sqlModelEdgePair),
                Set::add))
        .get(key);
  }

  private UnaryOperator<Map<SQL, NavigableMap<EdgeKey, ModelEdge>>>
      delegateMapUpdaterFunctionForAtomicUpdatesFollowingSetMembershipAndMembershipResultAction(
          SQL key,
          NavigableMap<EdgeKey, ModelEdge> value,
          BiPredicate<Set<Pair<SQL, ModelEdge>>, Pair<SQL, ModelEdge>> setMembershipPredicate,
          BiConsumer<Set<Pair<SQL, ModelEdge>>, Pair<SQL, ModelEdge>> setMembershipResultAction) {
    return currentMap -> {
      value.forEach(
          (edgeKey, modelEdge) -> checkModelEdgeOfTypeAssignableToSQLComponent(key, modelEdge));
      checkUpdatedModelEdgeMapCountAppropriateForSQLComponentType(value, key);
      Set<Pair<SQL, ModelEdge>> currentSetOfPairsInMap =
          convertCurrentMapIntoSetOfSQLComponentKeyModelEdgePairs(currentMap);
      value.forEach(
          (edgeKey, modelEdge) -> {
            createSQLExpressionPolymorphicIntegrityFunctionFollowingPredicateTakingAction(
                    setMembershipPredicate, setMembershipResultAction)
                .apply(currentSetOfPairsInMap, Pair.create(key, modelEdge));
          });
      return currentSetOfPairsInMap.stream()
          .collect(
              Collectors.groupingByConcurrent(
                  Pair::first,
                  Collectors.collectingAndThen(
                      Collectors.toList(),
                      this::createImmutableNavigableMapOfEdgeKeyAndModelEdges)))
          .entrySet()
          .stream()
          .reduce(
              ImmutableMap.<SQL, NavigableMap<EdgeKey, ModelEdge>>builder(),
              Builder::put,
              (builder, builder2) -> builder2)
          .build();
    };
  }

  private NavigableMap<EdgeKey, ModelEdge> createImmutableNavigableMapOfEdgeKeyAndModelEdges(
      List<Pair<SQL, ModelEdge>> pairs) {
    return pairs.stream()
        .map(Pair::second)
        .reduce(
            ImmutableSortedMap.<EdgeKey, ModelEdge>orderedBy(EdgeKey.comparator()),
            (edgeMapBuilder, modelEdge) -> edgeMapBuilder.put(modelEdge.edgeKey(), modelEdge),
            (pairModelEdgeBuilder, pairModelEdgeBuilder2) -> pairModelEdgeBuilder2)
        .build();
  }

  private Set<Pair<SQL, ModelEdge>> convertCurrentMapIntoSetOfSQLComponentKeyModelEdgePairs(
      Map<SQL, NavigableMap<EdgeKey, ModelEdge>> currentMap) {
    return currentMap.entrySet().stream()
        .map(entry -> Pair.create(entry.getKey(), entry.getValue().values()))
        .flatMap(
            entry ->
                entry.second().stream().map(modelEdge -> Pair.create(entry.first(), modelEdge)))
        .collect(Collectors.toSet());
  }

  public NavigableMap<EdgeKey, ModelEdge> putEdge(SQL key, ModelEdge edge) {
    NavigableMap<EdgeKey, ModelEdge> modelEdgeNavigableMap =
        get(Objects.requireNonNull(key, "key"));
    Objects.requireNonNull(edge, "edge");
    if (modelEdgeNavigableMap == null) {
      TreeMap<EdgeKey, ModelEdge> modelEdgeTreeMap = new TreeMap<>(EdgeKey.comparator());
      modelEdgeTreeMap.put(edge.edgeKey(), edge);
      return put(key, modelEdgeTreeMap);
    } else {
      TreeMap<EdgeKey, ModelEdge> modelEdgeTreeMap = new TreeMap<>(delegate.get().get(key));
      modelEdgeTreeMap.put(edge.edgeKey(), edge);
      return put(key, modelEdgeTreeMap);
    }
  }

  @Override
  public NavigableMap<EdgeKey, ModelEdge> remove(Object key) {
    Objects.requireNonNull(key, "key");
    return delegate
        .getAndUpdate(
            currentMap -> {
              if (key instanceof SQL && get(key) != null) {
                NavigableMap<EdgeKey, ModelEdge> currentValueForKey = get(key);
                return delegateMapUpdaterFunctionForAtomicUpdatesFollowingSetMembershipAndMembershipResultAction(
                        (SQL) key, currentValueForKey, Set::contains, Set::remove)
                    .apply(currentMap);
              }
              return currentMap;
            })
        .get(key);
  }

  @Override
  public void putAll(Map<? extends SQL, ? extends NavigableMap<EdgeKey, ModelEdge>> m) {
    Objects.requireNonNull(m, "map supplied");
    m.forEach(this::put);
  }

  @Override
  public void clear() {
    delegate.getAndUpdate(sqlNavigableMapMap -> ImmutableMap.of());
  }

  @Override
  public Set<SQL> keySet() {
    return delegate.get().keySet();
  }

  @Override
  public Collection<NavigableMap<EdgeKey, ModelEdge>> values() {
    return delegate.get().values();
  }

  @Override
  public Set<Entry<SQL, NavigableMap<EdgeKey, ModelEdge>>> entrySet() {
    return delegate.get().entrySet();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    QueryCriteriaEdgesProcessingMap that = (QueryCriteriaEdgesProcessingMap) o;
    return delegate.equals(that.delegate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(delegate);
  }

  @Override
  public boolean remove(Object key, Object value) {
    int initialHash = delegate.hashCode();
    delegate.getAndUpdate(
        currentMap -> {
          if (key instanceof SQL && get(key) != null) {
            NavigableMap<EdgeKey, ModelEdge> currentValueForKey = get(key);
            if (currentValueForKey.equals(Objects.requireNonNull(value, "value"))) {
              return delegateMapUpdaterFunctionForAtomicUpdatesFollowingSetMembershipAndMembershipResultAction(
                      (SQL) key, currentValueForKey, Set::contains, Set::remove)
                  .apply(currentMap);
            }
          }
          return currentMap;
        });
    int currentHash = delegate.hashCode();
    return initialHash != currentHash;
  }

  @Override
  public NavigableMap<EdgeKey, ModelEdge> getOrDefault(
      Object key, NavigableMap<EdgeKey, ModelEdge> defaultValue) {
    return delegate
        .get()
        .getOrDefault(
            Objects.requireNonNull(key, "key"),
            Objects.requireNonNull(defaultValue, "defaultValue"));
  }

  @Override
  public void forEach(BiConsumer<? super SQL, ? super NavigableMap<EdgeKey, ModelEdge>> action) {
    delegate.get().forEach(Objects.requireNonNull(action, "action"));
  }

  @Override
  public void replaceAll(
      BiFunction<
              ? super SQL,
              ? super NavigableMap<EdgeKey, ModelEdge>,
              ? extends NavigableMap<EdgeKey, ModelEdge>>
          function) {
    keySet()
        .forEach(
            sql -> {
              Optional.ofNullable(get(sql))
                  .map(currentMapValue -> function.apply(sql, currentMapValue))
                  .filter(Objects::nonNull)
                  .map(updatedMapValue -> put(sql, updatedMapValue));
            });
  }

  @Override
  public NavigableMap<EdgeKey, ModelEdge> putIfAbsent(
      SQL key, NavigableMap<EdgeKey, ModelEdge> value) {
    return delegate
        .getAndUpdate(
            currentMap -> {
              if (currentMap.get(Objects.requireNonNull(key, "key")) == null) {
                return delegateMapUpdaterFunctionForAtomicUpdatesFollowingSetMembershipAndMembershipResultAction(
                        key,
                        Objects.requireNonNull(value, "value"),
                        (pairs, sqlModelEdgePair) -> !pairs.contains(sqlModelEdgePair),
                        Set::add)
                    .apply(currentMap);
              }
              return currentMap;
            })
        .get(key);
  }

  @Override
  public boolean replace(
      SQL key,
      NavigableMap<EdgeKey, ModelEdge> oldValue,
      NavigableMap<EdgeKey, ModelEdge> newValue) {
    return Optional.ofNullable(
            delegate
                .getAndUpdate(
                    currentMap ->
                        Optional.ofNullable(currentMap.get(key))
                            .filter(
                                currentMapValue ->
                                    currentMapValue.equals(
                                        Objects.requireNonNull(oldValue, "oldValue")))
                            .map(
                                currentMapValue ->
                                    delegateMapUpdaterFunctionForAtomicUpdatesFollowingSetMembershipAndMembershipResultAction(
                                            key,
                                            Objects.requireNonNull(newValue, "newValue"),
                                            (pairs, sqlModelEdgePair) ->
                                                !pairs.contains(sqlModelEdgePair),
                                            Set::add)
                                        .apply(currentMap))
                            .orElse(currentMap))
                .get(key))
        .map(mapValue -> mapValue.equals(oldValue))
        .orElse(Boolean.FALSE);
  }

  private BiFunction<Set<Pair<SQL, ModelEdge>>, Pair<SQL, ModelEdge>, Set<Pair<SQL, ModelEdge>>>
      createSQLExpressionPolymorphicIntegrityFunctionFollowingPredicateTakingAction(
          BiPredicate<Set<Pair<SQL, ModelEdge>>, Pair<SQL, ModelEdge>> setMembershipPredicate,
          BiConsumer<Set<Pair<SQL, ModelEdge>>, Pair<SQL, ModelEdge>> setMembershipResultAction) {
    return (pairs, sqlModelEdgePair) -> {
      if (setMembershipPredicate.test(pairs, sqlModelEdgePair)) {
        setMembershipResultAction.accept(pairs, sqlModelEdgePair);
      }
      if (sqlModelEdgePair.first().equals(SQL.ROOT)
          && setMembershipPredicate.test(pairs, Pair.create(SQL.FROM, sqlModelEdgePair.second()))) {
        setMembershipResultAction.accept(pairs, Pair.create(SQL.FROM, sqlModelEdgePair.second()));
      }
      if (sqlModelEdgePair.first().equals(SQL.JOIN)
          && setMembershipPredicate.test(pairs, Pair.create(SQL.FROM, sqlModelEdgePair.second()))) {
        setMembershipResultAction.accept(pairs, Pair.create(SQL.FROM, sqlModelEdgePair.second()));
      }
      if (sqlModelEdgePair.first().equals(SQL.FROM)
          && sqlModelEdgePair.second() instanceof RootFromVertex
          && setMembershipPredicate.test(pairs, Pair.create(SQL.ROOT, sqlModelEdgePair.second()))) {
        setMembershipResultAction.accept(pairs, Pair.create(SQL.ROOT, sqlModelEdgePair.second()));
      }
      if (sqlModelEdgePair.first().equals(SQL.FROM)
          && sqlModelEdgePair.second() instanceof JoinEdge
          && setMembershipPredicate.test(pairs, Pair.create(SQL.JOIN, sqlModelEdgePair.second()))) {
        setMembershipResultAction.accept(pairs, Pair.create(SQL.JOIN, sqlModelEdgePair.second()));
      }
      return pairs;
    };
  }

  @Override
  public NavigableMap<EdgeKey, ModelEdge> computeIfAbsent(
      SQL key, Function<? super SQL, ? extends NavigableMap<EdgeKey, ModelEdge>> mappingFunction) {
    return delegate
        .getAndUpdate(
            currentMap -> {
              return currentMap.get(Objects.requireNonNull(key, "key")) != null
                  ? currentMap
                  : delegateMapUpdaterFunctionForAtomicUpdatesFollowingSetMembershipAndMembershipResultAction(
                          key,
                          Objects.requireNonNull(mappingFunction, "mappingFunction").apply(key),
                          (pairs, sqlModelEdgePair) -> !pairs.contains(sqlModelEdgePair),
                          Set::add)
                      .apply(currentMap);
            })
        .get(key);
  }

  @Override
  public NavigableMap<EdgeKey, ModelEdge> computeIfPresent(
      SQL key,
      BiFunction<
              ? super SQL,
              ? super NavigableMap<EdgeKey, ModelEdge>,
              ? extends NavigableMap<EdgeKey, ModelEdge>>
          remappingFunction) {
    return delegate
        .getAndUpdate(
            currentMap -> {
              return Optional.ofNullable(currentMap.get(Objects.requireNonNull(key, "key")))
                  .map(
                      currentValue ->
                          Objects.requireNonNull(remappingFunction, "remappingFunction")
                              .apply(key, currentValue))
                  .map(
                      possiblyUpdatedValue ->
                          delegateMapUpdaterFunctionForAtomicUpdatesFollowingSetMembershipAndMembershipResultAction(
                                  key,
                                  possiblyUpdatedValue,
                                  (pairs, sqlModelEdgePair) -> !pairs.contains(sqlModelEdgePair),
                                  Set::add)
                              .apply(currentMap))
                  .orElse(currentMap);
            })
        .get(key);
  }

  @Override
  public NavigableMap<EdgeKey, ModelEdge> compute(
      SQL key,
      BiFunction<
              ? super SQL,
              ? super NavigableMap<EdgeKey, ModelEdge>,
              ? extends NavigableMap<EdgeKey, ModelEdge>>
          remappingFunction) {
    Objects.requireNonNull(remappingFunction, "remappingFunction");
    return delegate
        .getAndUpdate(
            currentMap -> {
              return Optional.ofNullable(currentMap.get(Objects.requireNonNull(key, "key")))
                  .map(currentValue -> remappingFunction.apply(key, currentValue))
                  .map(
                      possiblyUpdatedValue ->
                          delegateMapUpdaterFunctionForAtomicUpdatesFollowingSetMembershipAndMembershipResultAction(
                                  key,
                                  possiblyUpdatedValue,
                                  (pairs, sqlModelEdgePair) -> !pairs.contains(sqlModelEdgePair),
                                  Set::add)
                              .apply(currentMap))
                  .orElseGet(
                      () ->
                          Optional.ofNullable(remappingFunction.apply(key, null))
                              .map(
                                  remappedValue ->
                                      delegateMapUpdaterFunctionForAtomicUpdatesFollowingSetMembershipAndMembershipResultAction(
                                              key,
                                              remappedValue,
                                              (pairs, sqlModelEdgePair) ->
                                                  !pairs.contains(sqlModelEdgePair),
                                              Set::add)
                                          .apply(currentMap))
                              .orElse(currentMap));
            })
        .get(key);
  }

  @Override
  public NavigableMap<EdgeKey, ModelEdge> merge(
      SQL key,
      NavigableMap<EdgeKey, ModelEdge> value,
      BiFunction<
              ? super NavigableMap<EdgeKey, ModelEdge>,
              ? super NavigableMap<EdgeKey, ModelEdge>,
              ? extends NavigableMap<EdgeKey, ModelEdge>>
          remappingFunction) {
    Objects.requireNonNull(remappingFunction, "remappingFunction");
    return delegate
        .getAndUpdate(
            currentMap -> {
              Optional<NavigableMap<EdgeKey, ModelEdge>> updatedMapIfPresent =
                  Optional.ofNullable(currentMap.get(Objects.requireNonNull(key, "key")))
                      .map(currentMapValue -> remappingFunction.apply(currentMapValue, value));
              return delegateMapUpdaterFunctionForAtomicUpdatesFollowingSetMembershipAndMembershipResultAction(
                      key,
                      updatedMapIfPresent.orElse(value),
                      (pairs, sqlModelEdgePair) -> !pairs.contains(sqlModelEdgePair),
                      Set::add)
                  .apply(currentMap);
            })
        .get(key);
  }

  private void checkUpdatedModelEdgeMapCountAppropriateForSQLComponentType(
      NavigableMap<EdgeKey, ModelEdge> modelEdgeNavigableMap, SQL key) {
    if (key.equals(SQL.ROOT) && modelEdgeNavigableMap.size() > 1) {
      Supplier<String> messageSupplier =
          () ->
              String.format(
                  "there can only be one RootFromVertex in this sql component mapping: [ %s ] ",
                  modelEdgeNavigableMap.values().stream()
                      .map(ModelEdge::parentPath)
                      .map(ModelPath::uri)
                      .map(URI::toString)
                      .collect(Collectors.joining(",")));
      throw new IllegalArgumentException(messageSupplier.get());
    }
  }

  private void checkModelEdgeOfTypeAssignableToSQLComponent(SQL key, ModelEdge edge) {
    if (!key.getModelEdgeType().isAssignableFrom(edge.getClass())) {
      Supplier<String> messageSupplier =
          () ->
              String.format(
                  "model edge [ %s ] is not of a type assignable to sql component model edge class: [ %s: %s ]",
                  edge, key.name(), key.getModelEdgeType().getName());
      throw new IllegalArgumentException(messageSupplier.get());
    }
  }

  public Optional<RootFromVertex> getRootIfAdded() {
    return Optional.of(delegate.get())
        .filter(delegateMap -> delegateMap.containsKey(QueryModelGraph.SQL.ROOT))
        .map(delegateMap -> delegateMap.get(QueryModelGraph.SQL.ROOT).firstEntry().getValue())
        .filter(modelEdge -> modelEdge instanceof RootFromVertex)
        .map(modelEdge -> (RootFromVertex) modelEdge);
  }

  @Override
  public boolean isEmpty() {
    return delegate.get().isEmpty();
  }
}
