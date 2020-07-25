package io.github.anticipasean.ent;

import cyclops.control.Option;
import cyclops.data.ImmutableMap;
import cyclops.data.tuple.Tuple2;
import io.github.anticipasean.ent.pattern.EntPattern;
import io.github.anticipasean.ent.pattern.Pattern;
import io.github.anticipasean.ent.state.EmptyEnt;
import io.github.anticipasean.ent.state.FilledEnt;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;


public interface Ent<K, V> extends Iterable<Tuple2<K, V>> {


    static <K, V> Ent<K, V> empty() {
        return EmptyEnt.emptyEnt();
    }

    static <K, V> Ent<K, V> fromImmutableMap(ImmutableMap<K, V> immutableMap) {
        return Option.ofNullable(immutableMap)
                     .filterNot(ImmutableMap::isEmpty)
                     .fold(FilledEnt::new,
                           EmptyEnt::emptyEnt);
    }

    /**
     * Ent backing data structure
     */
    ImmutableMap<K, V> toImmutableMap();

    /**
     * Ent Specific Methods
     */

    default <R> Option<R> matchGet(K key,
                                   Pattern<V, R> pattern) {
        return toImmutableMap().get(key)
                               .map(Pattern.mapper(pattern));
    }

    default <R> Ent<K, R> matchMap(Pattern<V, R> pattern){
        return fromImmutableMap(toImmutableMap().map(Pattern.mapper(pattern)));
    }

//    default <K2, V2> Ent<K2, V2> matchFlatMap(EntPattern<? extends K, ? extends V, ? extends K2, ? extends V2> entPattern){
//        return fromImmutableMap(toImmutableMap().bimap(EntPattern))
//    }
    // matchStream
    // matchBiMap
    // matchIdMap
    // matchFilter
    // matchFold
    // matchIterableGet(ID id, Function<C, Iterable<E>> func

    //    <R> Stream<R> matchExtract(K id,
    //                               Pattern<Object, R> pattern);

    /**
     * Iterable methods
     */
    default int size() {
        return toImmutableMap().size();
    }

    @Override
    default Iterator<Tuple2<K, V>> iterator() {
        return toImmutableMap().iterator();
    }

    /**
     * Monadic Methods: Mappers
     */

    default <R> Ent<K, R> map(Function<? super V, ? extends R> fn) {
        Objects.requireNonNull(fn,
                               "fn");
        return fromImmutableMap(toImmutableMap().map(fn));
    }

    default <R> Ent<K, R> mapValues(Function<? super V, ? extends R> mapper) {
        Objects.requireNonNull(mapper,
                               "mapper");
        return fromImmutableMap(toImmutableMap().map(mapper));
    }

    default <R> Ent<R, V> mapKeys(Function<? super K, ? extends R> mapper) {
        Objects.requireNonNull(mapper,
                               "mapper");
        return fromImmutableMap(toImmutableMap().mapKeys(mapper));
    }

    default <R1, R2> Ent<R1, R2> bimap(BiFunction<? super K, ? super V, ? extends Tuple2<R1, R2>> mapper) {
        Objects.requireNonNull(mapper,
                               "mapper");
        return fromImmutableMap(toImmutableMap().bimap(mapper));
    }

    default <R1, R2> Ent<R1, R2> bimap(Function<? super K, ? extends R1> keyMapper,
                                       Function<? super V, ? extends R2> valueMapper) {
        Objects.requireNonNull(keyMapper,
                               "keyMapper");
        Objects.requireNonNull(valueMapper,
                               "valueMapper");
        return fromImmutableMap(toImmutableMap().bimap(keyMapper,
                                                       valueMapper));
    }

    default <K2, V2> Ent<K2, V2> flatMap(BiFunction<? super K, ? super V, ? extends ImmutableMap<K2, V2>> mapper) {
        Objects.requireNonNull(mapper,
                               "mapper");
        return fromImmutableMap(toImmutableMap().flatMap(mapper));
    }

    default <K2, V2> Ent<K2, V2> concatMap(BiFunction<? super K, ? super V, ? extends Iterable<Tuple2<K2, V2>>> mapper) {
        Objects.requireNonNull(mapper,
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
        Objects.requireNonNull(predicate,
                               "predicate");
        return fromImmutableMap(toImmutableMap().filter(predicate));
    }

    default Ent<K, V> filterKeys(Predicate<? super K> predicate) {
        Objects.requireNonNull(predicate,
                               "predicate");
        return fromImmutableMap(toImmutableMap().filterKeys(predicate));
    }

    default Ent<K, V> filterValues(Predicate<? super V> predicate) {
        Objects.requireNonNull(predicate,
                               "predicate");
        return fromImmutableMap(toImmutableMap().filterValues(predicate));
    }

    default Ent<K, V> filterNot(Predicate<? super Tuple2<K, V>> predicate) {
        Objects.requireNonNull(predicate,
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
        Objects.requireNonNull(sep,
                               "sep");
        return toImmutableMap().join(sep);
    }

    default String join(String sep,
                        String start,
                        String end) {
        Objects.requireNonNull(sep,
                               "sep");
        Objects.requireNonNull(start,
                               "start");
        Objects.requireNonNull(end,
                               "end");
        return toImmutableMap().join(sep,
                                     start,
                                     end);
    }

    /**
     * Monadic Methods: Alternative Paths
     */

    //    Ent<K, V> onEmpty(Tuple2<K, V> value);
    //
    //    Ent<K, V> onEmptyGet(Supplier<? extends Tuple2<K, V>> supplier);
    //
    //    <X extends Throwable> Try<Ent<K, V>, X> onEmptyTry(Supplier<? extends X> supplier);
    //
    //    Ent<K, V> onEmptySwitch(Supplier<? extends Ent<K, V>> supplier);

    /**
     * Monadic Methods: Reduction
     */

    //    <R> R foldMap(Reducer<R, Tuple2<K, V>> reducer);
    //
    //    <R> R foldMap(Function<? super Tuple2<K, V>, ? extends R> mapper,
    //                  Monoid<R> reducer);
    //
    //    Option<Tuple2<K, V>> foldLeft(BinaryOperator<Tuple2<K, V>> accumulator);
    //
    //    <U> U foldLeft(U identity,
    //                   BiFunction<U, ? super Tuple2<K, V>, U> accumulator);
    //
    //    <U> U foldLeft(U identity,
    //                   BiFunction<U, ? super Tuple2<K, V>, U> accumulator,
    //                   BinaryOperator<U> combiner);
    //
    //    Tuple2<K, V> foldLeft(Tuple2<K, V> identity,
    //                          BinaryOperator<Tuple2<K, V>> accumulator);
    //
    //    Tuple2<K, V> foldLeft(Monoid<Tuple2<K, V>> reducer);
    //
    //    Seq<Tuple2<K, V>> foldLeft(Iterable<? extends Monoid<Tuple2<K, V>>> reducers);
    //
    //    Tuple2<K, V> foldRight(Monoid<Tuple2<K, V>> reducer);
    //
    //    Tuple2<K, V> foldRight(Tuple2<K, V> identity,
    //                           BinaryOperator<Tuple2<K, V>> accumulator);
    //
    //    <U> U foldRight(U identity,
    //                    BiFunction<? super Tuple2<K, V>, ? super U, ? extends U> accumulator);
    //
    //    <R> R foldMapRight(Reducer<R, Tuple2<K, V>> reducer);

    /**
     * Map Methods
     */

    //    Option<V> get(K key);
    //
    //    V getOrElse(K key,
    //                V alt);
    //
    //    V getOrElseGet(K key,
    //                   Supplier<? extends V> alt);
    //
    //    Ent<K, V> put(K key,
    //                  V value);
    //
    //    Ent<K, V> put(Tuple2<K, V> keyAndValue);
    //
    //    Ent<K, V> putAll(PersistentMap<? extends K, ? extends V> map);
    //
    //    Ent<K, V> remove(K key);
    //
    //    Ent<K, V> removeAll(K... keys);
    //
    //    ReactiveSeq<K> keys();
    //
    //    ReactiveSeq<V> values();
    //
    //    Ent<K, V> removeAllKeys(Iterable<? extends K> keys);
    //
    //    boolean containsValue(V value);
    //
    //    boolean isEmpty();
    //
    //    boolean containsKey(K key);
    //
    //    boolean contains(Tuple2<K, V> t);

    /**
     * Testing Methods
     */

    //    boolean allMatch(Predicate<? super Tuple2<K, V>> c);
    //
    //    boolean anyMatch(Predicate<? super Tuple2<K, V>> c);
    //
    //    boolean noneMatch(Predicate<? super Tuple2<K, V>> c);

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
    //
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
}
