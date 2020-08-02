package io.github.anticipasean.ent;

import static java.util.Objects.requireNonNull;

import com.oath.cyclops.types.persistent.PersistentMap;
import cyclops.control.Option;
import cyclops.control.Try;
import cyclops.data.HashMap;
import cyclops.data.ImmutableMap;
import cyclops.data.TreeMap;
import cyclops.data.tuple.Tuple2;
import cyclops.function.Reducer;
import cyclops.reactive.ReactiveSeq;
import io.github.anticipasean.ent.func.single.Pattern1;
import io.github.anticipasean.ent.pattern.KeyValuePattern;
import io.github.anticipasean.ent.pattern.ValuePattern;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public interface Ent<K, V> extends Iterable<Tuple2<K, V>> {

    static final boolean PARALLEL = true;

    static <K, V> Ent<K, V> of(K key,
                               V value) {
        requireNonNull(key,
                       "key");
        requireNonNull(value,
                       "value");
        return fromImmutableMap(HashMap.of(key,
                                           value));
    }

    static <K, V> Ent<K, V> ofSorted(K key,
                                     V value,
                                     Comparator<K> keyComparator) {
        requireNonNull(key,
                       "key");
        requireNonNull(value,
                       "value");
        return fromImmutableMap(TreeMap.of(keyComparator,
                                           key,
                                           value));
    }

    static <K, V> Ent<K, V> empty() {
        return fromImmutableMap(HashMap.empty());
    }

    static <K, V> Ent<K, V> emptySorted(Comparator<K> keyComparator) {
        requireNonNull(keyComparator,
                       "keyComparator");
        return fromImmutableMap(TreeMap.empty(keyComparator));
    }

    static <K, V> Ent<K, V> fromImmutableMap(ImmutableMap<K, V> immutableMap) {
        return Option.ofNullable(immutableMap)
                     .fold(EntImpl::new,
                           () -> new EntImpl<>(HashMap.empty()));
    }

    /**
     * Ent backing data structure
     */
    ImmutableMap<K, V> toImmutableMap();

    /**
     * Ent Specific Methods
     */

    default <R> Option<R> matchGet(K key,
                                   Pattern1<V, R> valuePattern) {
        return toImmutableMap().get(key)
                               .map(Pattern1.asMapper(valuePattern));
    }

//    default <R> Option<Tuple2<K, R>> matchGet(K key,
//                                              KeyValuePattern<K, V, R> keyValuePattern) {
//        return toImmutableMap().get(key)
//                               .fold(v -> Option.of(Tuple2.of(key,
//                                                              v))
//                                                .map(KeyValuePattern.tupleMapper(keyValuePattern)),
//                                     Option::none);
//    }

    default <R> Ent<K, R> matchMap(ValuePattern<V, R> valuePattern) {
        return fromImmutableMap(toImmutableMap().map(ValuePattern.mapper(valuePattern)));
    }

    default <R> Ent<K, R> matchFlatMap(ValuePattern<V, Ent<K, R>> valuePattern) {
        requireNonNull(valuePattern,
                       "valuePattern");
        return fromImmutableMap(toImmutableMap().flatMap((k, v) -> ValuePattern.mapper(valuePattern)
                                                                               .apply(v)
                                                                               .toImmutableMap()));
    }

    default <R> ReactiveSeq<Tuple2<K, R>> matchMapToReactiveSeqStream(ValuePattern<V, R> valuePattern) {
        requireNonNull(valuePattern,
                       "valuePattern");
        return toImmutableMap().map(ValuePattern.mapper(valuePattern))
                               .stream();
    }

    default <R> Stream<Tuple2<K, R>> matchMapToJavaUtilStream(ValuePattern<V, R> valuePattern) {
        requireNonNull(valuePattern,
                       "valuePattern");
        return StreamSupport.stream(toImmutableMap().map(ValuePattern.mapper(valuePattern))
                                                    .stream()
                                                    .spliterator(),
                                    PARALLEL);
    }

    default <R> Ent<K, R> matchBiMap(KeyValuePattern<K, V, R> keyValuePattern) {
        requireNonNull(keyValuePattern,
                       "keyValuePattern");
        return fromImmutableMap(toImmutableMap().bimap(KeyValuePattern.pairMapper(keyValuePattern)));
    }

    default <R> ReactiveSeq<Tuple2<K, R>> matchBiMapToReactiveSeqStream(KeyValuePattern<K, V, R> keyValuePattern) {
        requireNonNull(keyValuePattern,
                       "keyValuePattern");
        return toImmutableMap().bimap(KeyValuePattern.pairMapper(keyValuePattern))
                               .stream();
    }

    default <R> Stream<Tuple2<K, R>> matchBiMapToJavaUtilStream(KeyValuePattern<K, V, R> keyValuePattern) {
        requireNonNull(keyValuePattern,
                       "keyValuePattern");
        return StreamSupport.stream(toImmutableMap().bimap(KeyValuePattern.pairMapper(keyValuePattern))
                                                    .stream()
                                                    .spliterator(),
                                    PARALLEL);
    }

    // matchIdMap
    // matchFilter
    // matchFold
    // matchIterableGet(ID id, Function<C, Iterable<E>> func

    default <R> Stream<R> matchExtractValueStream(K key,
                                   ValuePattern<V, R> pattern){
        return null;
    }

    /**
     * Iterable methods
     */

    @Override
    default Iterator<Tuple2<K, V>> iterator() {
        return toImmutableMap().iterator();
    }

    /**
     * Monadic Methods: Mappers
     */

    default <R> Ent<K, R> map(Function<? super V, ? extends R> fn) {
        requireNonNull(fn,
                       "fn");
        return fromImmutableMap(toImmutableMap().map(fn));
    }

    default <R> Ent<K, R> mapValues(Function<? super V, ? extends R> mapper) {
        requireNonNull(mapper,
                       "mapper");
        return fromImmutableMap(toImmutableMap().map(mapper));
    }

    default <R> Ent<R, V> mapKeys(Function<? super K, ? extends R> mapper) {
        requireNonNull(mapper,
                       "mapper");
        return fromImmutableMap(toImmutableMap().mapKeys(mapper));
    }

    default <R1, R2> Ent<R1, R2> bimap(BiFunction<? super K, ? super V, ? extends Tuple2<R1, R2>> mapper) {
        requireNonNull(mapper,
                       "mapper");
        return fromImmutableMap(toImmutableMap().bimap(mapper));
    }

    default <R1, R2> Ent<R1, R2> bimap(Function<? super K, ? extends R1> keyMapper,
                                       Function<? super V, ? extends R2> valueMapper) {
        requireNonNull(keyMapper,
                       "keyMapper");
        requireNonNull(valueMapper,
                       "valueMapper");
        return fromImmutableMap(toImmutableMap().bimap(keyMapper,
                                                       valueMapper));
    }

    default <K2, V2> Ent<K2, V2> flatMap(BiFunction<? super K, ? super V, ? extends Ent<K2, V2>> mapper) {
        requireNonNull(mapper,
                       "mapper");
        return fromImmutableMap(toImmutableMap().flatMap((k, v) -> mapper.apply(k,
                                                                                v)
                                                                         .toImmutableMap()));
    }

    default <K2, V2> Ent<K2, V2> flatMap(Function<Tuple2<? super K, ? super V>, ? extends Ent<K2, V2>> mapper) {
        requireNonNull(mapper,
                       "mapper");
        return fromImmutableMap(toImmutableMap().flatMap((k, v) -> mapper.apply(Tuple2.of(k,
                                                                                          v))
                                                                         .toImmutableMap()));
    }

    default <K2, V2> Ent<K2, V2> concatMap(BiFunction<? super K, ? super V, ? extends Iterable<Tuple2<K2, V2>>> mapper) {
        requireNonNull(mapper,
                       "mapper");
        return fromImmutableMap(toImmutableMap().concatMap(mapper));
    }

    /**
     * Monadic Methods: Combinators
     */
    //
    //    <K2, V2> Two<K, V, K2, V2> merge(ImmutableMap<K2, V2> one);
    //
    //    <K2, V2, K3, V3> Three<K, V, K2, V2, K3, V3> merge(Two<K2, V2, K3, V3> two);

    /**
     * Monadic Methods: Streams
     */

    //    ReactiveSeq<Tuple2<K, V>> stream();
    //
    //    <R> R collect(Supplier<R> supplier,
    //                  BiConsumer<R, ? super Tuple2<K, V>> accumulator,
    //                  BiConsumer<R, R> combiner);
    //
    //    <R1, R2, A1, A2> Tuple2<R1, R2> collect(Collector<? super Tuple2<K, V>, A1, R1> c1,
    //                                            Collector<? super Tuple2<K, V>, A2, R2> c2);
    //
    //    <R1, R2, R3, A1, A2, A3> Tuple3<R1, R2, R3> collect(Collector<? super Tuple2<K, V>, A1, R1> c1,
    //                                                        Collector<? super Tuple2<K, V>, A2, R2> c2,
    //                                                        Collector<? super Tuple2<K, V>, A3, R3> c3);

    /**
     * Monadic Methods: Filters
     */

    default Ent<K, V> filter(Predicate<? super Tuple2<K, V>> predicate) {
        requireNonNull(predicate,
                       "predicate");
        return fromImmutableMap(toImmutableMap().filter(predicate));
    }

    default Ent<K, V> filterKeys(Predicate<? super K> predicate) {
        requireNonNull(predicate,
                       "predicate");
        return fromImmutableMap(toImmutableMap().filterKeys(predicate));
    }

    default Ent<K, V> filterValues(Predicate<? super V> predicate) {
        requireNonNull(predicate,
                       "predicate");
        return fromImmutableMap(toImmutableMap().filterValues(predicate));
    }

    default Ent<K, V> filterNot(Predicate<? super Tuple2<K, V>> predicate) {
        requireNonNull(predicate,
                       "predicate");
        return fromImmutableMap(toImmutableMap().filterNot(predicate));
    }

    default Ent<K, V> notNull() {
        return fromImmutableMap(toImmutableMap().notNull());
    }

    /**
     * Monadic Methods: Debuggers
     */

    default Ent<K, V> peek(Consumer<? super V> peeker) {
        return fromImmutableMap(toImmutableMap().peek(peeker));
    }

    default Ent<K, V> bipeek(Consumer<? super K> keyPeeker,
                             Consumer<? super V> valuePeeker) {
        return fromImmutableMap(toImmutableMap().bipeek(keyPeeker,
                                                        valuePeeker));
    }

    default Ent<K, V> bipeek(BiConsumer<? super K, ? super V> peeker) {
        return fromImmutableMap(toImmutableMap().bimap((k, v) -> {
            peeker.accept(k,
                          v);
            return Tuple2.of(k,
                             v);
        }));
    }

    default String mkString() {
        return toImmutableMap().mkString();
    }

    default String join() {
        return toImmutableMap().join();
    }

    default String join(String sep) {
        requireNonNull(sep,
                       "sep");
        return toImmutableMap().join(sep);
    }

    default String join(String sep,
                        String start,
                        String end) {
        requireNonNull(sep,
                       "sep");
        requireNonNull(start,
                       "start");
        requireNonNull(end,
                       "end");
        return toImmutableMap().join(sep,
                                     start,
                                     end);
    }

    /**
     * Monadic Methods: Alternative Paths When Empty
     */

    default Ent<K, V> onEmpty(Tuple2<K, V> value) {
        requireNonNull(value,
                       "value");
        return fromImmutableMap(toImmutableMap().onEmpty(value));
    }

    default Ent<K, V> onEmptyGet(Supplier<? extends Tuple2<K, V>> supplier) {
        requireNonNull(supplier,
                       "supplier");
        return fromImmutableMap(toImmutableMap().onEmptyGet(supplier));
    }

    default <X extends Throwable> Try<Ent<K, V>, X> onEmptyTry(Supplier<? extends X> supplier) {
        requireNonNull(supplier,
                       "supplier");
        return isEmpty() ? Try.failure(supplier.get()) : Try.success(this);
    }

    default Ent<K, V> onEmptySwitch(Supplier<? extends Ent<K, V>> supplier) {
        requireNonNull(supplier,
                       "supplier");
        return isEmpty() ? supplier.get() : this;
    }

    /**
     * Monadic Methods: Reduction
     */

    default <R> R foldMap(R zero,
                          BiFunction<K, V, R> mapper,
                          BinaryOperator<R> combiner) {
        requireNonNull(zero,
                       "zero");
        requireNonNull(mapper,
                       "mapper");
        requireNonNull(combiner,
                       "combiner");
        return toImmutableMap().foldMap(Reducer.of(zero,
                                                   combiner,
                                                   tuple2 -> mapper.apply(tuple2._1(),
                                                                          tuple2._2())));
    }

    default <R> R foldMap(R zero,
                          Function<Tuple2<K, V>, R> mapper,
                          BinaryOperator<R> combiner) {
        requireNonNull(zero,
                       "zero");
        requireNonNull(mapper,
                       "mapper");
        requireNonNull(combiner,
                       "combiner");
        return toImmutableMap().foldMap(Reducer.of(zero,
                                                   combiner,
                                                   mapper));
    }

    default Option<Tuple2<K, V>> foldLeft(BinaryOperator<Tuple2<K, V>> accumulator) {
        requireNonNull(accumulator,
                       "accumulator");
        return toImmutableMap().foldLeft(accumulator);
    }

    default <U> U foldLeft(U identity,
                           BiFunction<U, ? super Tuple2<K, V>, U> accumulator) {
        requireNonNull(identity,
                       "identity");
        requireNonNull(accumulator,
                       "accumulator");
        return toImmutableMap().foldLeft(identity,
                                         accumulator);
    }

    default <U> U foldLeft(U identity,
                           BiFunction<U, ? super Tuple2<K, V>, U> accumulator,
                           BinaryOperator<U> combiner) {
        requireNonNull(identity,
                       "identity");
        requireNonNull(accumulator,
                       "accumulator");
        requireNonNull(combiner,
                       "combiner");
        return toImmutableMap().foldLeft(identity,
                                         accumulator,
                                         combiner);
    }

    default Tuple2<K, V> foldLeft(Tuple2<K, V> identity,
                                  BinaryOperator<Tuple2<K, V>> accumulator) {
        requireNonNull(identity,
                       "identity");
        requireNonNull(accumulator,
                       "accumulator");
        return toImmutableMap().foldLeft(identity,
                                         accumulator);
    }

    default Tuple2<K, V> foldRight(Tuple2<K, V> identity,
                                   BinaryOperator<Tuple2<K, V>> accumulator) {
        requireNonNull(identity,
                       "identity");
        requireNonNull(accumulator,
                       "accumulator");
        return toImmutableMap().foldRight(identity,
                                          accumulator);
    }

    default <U> U foldRight(U identity,
                            BiFunction<? super Tuple2<K, V>, ? super U, ? extends U> accumulator) {
        requireNonNull(identity,
                       "identity");
        requireNonNull(accumulator,
                       "accumulator");
        return toImmutableMap().foldRight(identity,
                                          accumulator);
    }

    default <R> R foldMapRight(R zero,
                               BiFunction<K, V, R> mapper,
                               BinaryOperator<R> combiner) {
        requireNonNull(zero,
                       "zero");
        requireNonNull(mapper,
                       "mapper");
        requireNonNull(combiner,
                       "combiner");
        return toImmutableMap().foldMapRight(Reducer.of(zero,
                                                        combiner,
                                                        tuple2 -> mapper.apply(tuple2._1(),
                                                                               tuple2._2())));
    }

    /**
     * Map Data Structure Methods
     */

    default Option<V> get(K key) {
        requireNonNull(key,
                       "key");
        return toImmutableMap().get(key);
    }

    default V getOrElse(K key,
                        V alt) {
        requireNonNull(key,
                       "key");
        return toImmutableMap().getOrElse(key,
                                          alt);
    }

    default V getOrElseGet(K key,
                           Supplier<? extends V> alt) {
        requireNonNull(key,
                       "key");
        return toImmutableMap().getOrElseGet(key,
                                             alt);
    }

    default Ent<K, V> put(K key,
                          V value) {
        requireNonNull(key,
                       "key");
        requireNonNull(value,
                       "value");
        return fromImmutableMap(toImmutableMap().put(key,
                                                     value));
    }

    default Ent<K, V> put(Tuple2<K, V> keyAndValue) {

        requireNonNull(keyAndValue,
                       "keyAndValue");
        return fromImmutableMap(toImmutableMap().put(keyAndValue));
    }

    default Ent<K, V> putAll(PersistentMap<? extends K, ? extends V> map) {
        requireNonNull(map,
                       "map");
        return fromImmutableMap(toImmutableMap().putAll(map));
    }

    default Ent<K, V> remove(K key) {

        requireNonNull(key,
                       "key");
        return fromImmutableMap(toImmutableMap().remove(key));
    }

    default Ent<K, V> removeAll(K... keys) {

        requireNonNull(keys,
                       "keys");
        return fromImmutableMap(toImmutableMap().removeAll(keys));
    }

    default List<K> keysList() {
        return toImmutableMap().keys()
                               .toList();
    }

    default ReactiveSeq<K> keys() {
        return toImmutableMap().keys();
    }

    default List<V> valuesList() {
        return toImmutableMap().values()
                               .toList();
    }

    default ReactiveSeq<V> values() {
        return toImmutableMap().values();
    }

    default Ent<K, V> removeAllKeys(Iterable<? extends K> keys) {
        requireNonNull(keys,
                       "keys");
        return fromImmutableMap(toImmutableMap().removeAllKeys(keys));
    }

    default boolean containsValue(V value) {
        requireNonNull(value,
                       "value");
        return toImmutableMap().containsValue(value);
    }

    default boolean isEmpty() {
        return toImmutableMap().isEmpty();
    }

    default boolean containsKey(K key) {
        requireNonNull(key,
                       "key");
        return toImmutableMap().containsKey(key);
    }

    default boolean contains(Tuple2<K, V> tuple) {
        requireNonNull(tuple,
                       "tuple");
        return toImmutableMap().contains(tuple);
    }

    default int size() {
        return toImmutableMap().size();
    }

    /**
     * Testing Methods
     */

    default boolean allMatch(Predicate<? super Tuple2<K, V>> condition) {
        requireNonNull(condition,
                       "condition");
        return toImmutableMap().allMatch(condition);
    }

    default boolean anyMatch(Predicate<? super Tuple2<K, V>> condition) {
        requireNonNull(condition,
                       "condition");
        return toImmutableMap().anyMatch(condition);
    }

    default boolean noneMatch(Predicate<? super Tuple2<K, V>> condition) {
        requireNonNull(condition,
                       "condition");
        return toImmutableMap().noneMatch(condition);
    }

    /**
     * Fancy ForEach Comprehensions
     */

    //    <K1, K2, K3, K4, R1, R2, R3, R> ImmutableMap<K4, R> forEach4(Function<? super Tuple2<K, V>, ? extends Iterable<Tuple2<K1, R1>>> iterable1,
    //                                                                 BiFunction<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? extends Iterable<Tuple2<K2, R2>>> iterable2,
    //                                                                 Function3<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? super Tuple2<K2, R2>, ? extends Iterable<Tuple2<K3, R3>>> iterable3,
    //                                                                 Function4<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? super Tuple2<K2, R2>, ? super Tuple2<K3, R3>, ? extends Tuple2<K4, R>> yieldingFunction);
    //
    //    <K1, K2, K3, K4, R1, R2, R3, R> ImmutableMap<K4, R> forEach4(Function<? super Tuple2<K, V>, ? extends Iterable<Tuple2<K1, R1>>> iterable1,
    //                                                                 BiFunction<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? extends Iterable<Tuple2<K2, R2>>> iterable2,
    //                                                                 Function3<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? super Tuple2<K2, R2>, ? extends Iterable<Tuple2<K3, R3>>> iterable3,
    //                                                                 Function4<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? super Tuple2<K2, R2>, ? super Tuple2<K3, R3>, Boolean> filterFunction,
    //                                                                 Function4<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? super Tuple2<K2, R2>, ? super Tuple2<K3, R3>, ? extends Tuple2<K4, R>> yieldingFunction);
    //
    //    <K1, K2, K3, R1, R2, R> ImmutableMap<K3, R> forEach3(Function<? super Tuple2<K, V>, ? extends Iterable<Tuple2<K1, R1>>> iterable1,
    //                                                         BiFunction<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? extends Iterable<Tuple2<K2, R2>>> iterable2,
    //                                                         Function3<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? super Tuple2<K2, R2>, ? extends Tuple2<K3, R>> yieldingFunction);
    //
    //    <K1, K2, K3, R1, R2, R> ImmutableMap<K3, R> forEach3(Function<? super Tuple2<K, V>, ? extends Iterable<Tuple2<K1, R1>>> iterable1,
    //                                                         BiFunction<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? extends Iterable<Tuple2<K2, R2>>> iterable2,
    //                                                         Function3<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? super Tuple2<K2, R2>, Boolean> filterFunction,
    //                                                         Function3<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? super Tuple2<K2, R2>, ? extends Tuple2<K3, R>> yieldingFunction);
    //
    //    <K1, K2, R1, R> ImmutableMap<K2, R> forEach2(Function<? super Tuple2<K, V>, ? extends Iterable<Tuple2<K1, R1>>> iterable1,
    //                                                 BiFunction<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? extends Tuple2<K2, R>> yieldingFunction);
    //
    //    <K1, K2, R1, R> ImmutableMap<K2, R> forEach2(Function<? super Tuple2<K, V>, ? extends Iterable<Tuple2<K1, R1>>> iterable1,
    //                                                 BiFunction<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, Boolean> filterFunction,
    //                                                 BiFunction<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? extends Tuple2<K2, R>> yieldingFunction);

    /**
     * Transformers
     */

    //    Map<K, V> toJavaMap();
    //
    //
    //    <K, V> HashMap<K, V> toHashMap(Function<? super Tuple2<K, V>, ? extends K> keyMapper,
    //                                   Function<? super Tuple2<K, V>, ? extends V> valueMapper);
    //
    //
    //    <K, V> Map<K, V> toMap(Function<? super Tuple2<K, V>, ? extends K> keyMapper,
    //                           Function<? super Tuple2<K, V>, ? extends V> valueMapper);
    //
    //
    //    <R> HashMap<R, Tuple2<K, V>> toHashMap(Function<? super Tuple2<K, V>, ? extends R> keyMapper);
    //
    //    <R> Map<R, Tuple2<K, V>> toMap(Function<? super Tuple2<K, V>, ? extends R> keyMapper);
    //
    //    <T> Seq<T> toSeq(Function<? super Tuple2<? super K, ? super V>, ? extends T> fn);
    //
    //    <T> LazySeq<T> toLazySeq(Function<? super Tuple2<? super K, ? super V>, ? extends T> fn);
    //
    //    <U> Filters<U> ofType(Class<? extends U> type);
    //
    //    <R> R[] toArray(IntFunction<R[]> generator);
    //
    //    Object[] toArray();
    //
    //    <R> R to(Function<? super Iterable<? super Tuple2<K, V>>, ? extends R> fn);
    //
    //    BankersQueue<Tuple2<K, V>> bankersQueue();
    //
    //    TreeSet<Tuple2<K, V>> treeSet(Comparator<? super Tuple2<K, V>> comp);
    //
    //    HashSet<Tuple2<K, V>> hashSet();
    //
    //    Vector<Tuple2<K, V>> vector();
    //
    //    LazySeq<Tuple2<K, V>> lazySeq();
    //
    //    Seq<Tuple2<K, V>> seq();
    //
    //    NonEmptyList<Tuple2<K, V>> nonEmptyList(Supplier<Tuple2<K, V>> s);
    //
    //    <T extends Collection<Tuple2<K, V>>> T toCollection(Supplier<T> collectionFactory);
    //
    //    List<Tuple2<K, V>> toList();
    //
    //    Set<Tuple2<K, V>> toSet();

    /**
     * Stats Methods
     */

    //    long countDistinct();
    //
    //    <U> Option<Tuple2<K, V>> maxBy(Function<? super Tuple2<K, V>, ? extends U> function,
    //                                   Comparator<? super U> comparator);
    //
    //    <U extends Comparable<? super U>> Option<Tuple2<K, V>> maxBy(Function<? super Tuple2<K, V>, ? extends U> function);
    //
    //    <U extends Comparable<? super U>> Option<Tuple2<K, V>> minBy(Function<? super Tuple2<K, V>, ? extends U> function);
    //
    //    <U extends Comparable<? super U>> Option<Tuple2<K, V>> minBy(Function<? super Tuple2<K, V>, ? extends U> function,
    //                                                                 Comparator<? super U> comparator);
    //
    //    Option<Tuple2<K, V>> mode();
    //
    //    ReactiveSeq<Tuple2<Tuple2<K, V>, Integer>> occurrences();
    //
    //    double mean(ToDoubleFunction<Tuple2<K, V>> fn);
    //
    //    Option<Tuple2<K, V>> median();
    //
    //    Seq<Tuple2<Tuple2<K, V>, BigDecimal>> withPercentiles();
    //
    //    Option<Tuple2<K, V>> atPercentile(double percentile);
    //
    //    double variance(ToDoubleFunction<Tuple2<K, V>> fn);
    //
    //    double populationVariance(ToDoubleFunction<Tuple2<K, V>> fn);
    //
    //    double stdDeviation(ToDoubleFunction<Tuple2<K, V>> fn);
    //
    //    Option<Tuple2<K, V>> maximum(Comparator<? super Tuple2<K, V>> comparator);
    //
    //    Option<Tuple2<K, V>> minimum(Comparator<? super Tuple2<K, V>> comparator);
    //
    //    int sumInt(ToIntFunction<Tuple2<K, V>> fn);
    //
    //    double sumDouble(ToDoubleFunction<Tuple2<K, V>> fn);
    //
    //    long sumLong(ToLongFunction<Tuple2<K, V>> fn);
    //
    //    <R, A> R collect(Collector<? super Tuple2<K, V>, A, R> collector);
    //
    //    long count();
    //
    //
    //    <K1> HashMap<K1, Vector<Tuple2<K, V>>> groupBy(Function<? super Tuple2<K, V>, ? extends K1> classifier);
    //
    //    Option<Tuple2<K, V>> headOption();
    //
    //    boolean startsWith(Iterable<Tuple2<K, V>> iterable);
    //
    //    boolean endsWith(Iterable<Tuple2<K, V>> iterable);

    /**
     * Sampling Methods
     */

    //    Tuple2<K, V> firstValue(Tuple2<K, V> alt);
    //
    //    Tuple2<K, V> singleOrElse(Tuple2<K, V> alt);
    //
    //    Maybe<Tuple2<K, V>> single(Predicate<? super Tuple2<K, V>> predicate);
    //
    //    Maybe<Tuple2<K, V>> single();
    //
    //    Maybe<Tuple2<K, V>> takeOne();
    //
    //    Maybe<Tuple2<K, V>> elementAt(long index);
    //
    //    Maybe<Long> indexOf(Predicate<? super Tuple2<K, V>> pred);
    //
    //    Maybe<Long> lastIndexOf(Predicate<? super Tuple2<K, V>> pred);
    //
    //    Maybe<Long> indexOfSlice(Iterable<? extends Tuple2<K, V>> slice);
    //
    //    Maybe<Long> lastIndexOfSlice(Iterable<? extends Tuple2<K, V>> slice);
    //
    //    boolean atLeast(int num,
    //                    Predicate<? super Tuple2<K, V>> c);
    //
    //    boolean atMost(int num,
    //                   Predicate<? super Tuple2<K, V>> c);

    static final class EntImpl<K, V> implements Ent<K, V> {

        private final ImmutableMap<K, V> data;

        public EntImpl(ImmutableMap<K, V> data) {
            this.data = requireNonNull(data,
                                       "data");
        }

        @Override
        public ImmutableMap<K, V> toImmutableMap() {
            return data;
        }

    }
}
