package io.github.anticipasean.ent;

import static java.util.Objects.requireNonNull;

import com.oath.cyclops.types.persistent.PersistentMap;
import cyclops.companion.Streamable;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.control.Try;
import cyclops.data.BankersQueue;
import cyclops.data.HashMap;
import cyclops.data.HashSet;
import cyclops.data.ImmutableMap;
import cyclops.data.LazySeq;
import cyclops.data.NonEmptyList;
import cyclops.data.Seq;
import cyclops.data.TreeMap;
import cyclops.data.TreeSet;
import cyclops.data.Vector;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.function.Reducer;
import cyclops.reactive.ReactiveSeq;
import io.github.anticipasean.ent.pattern.VariantMapper;
import io.github.anticipasean.ent.pattern.pair.EntPattern2;
import io.github.anticipasean.ent.pattern.pair.Pattern2;
import io.github.anticipasean.ent.pattern.single.Pattern1;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.Collectors;
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

    static <K, V> Ent<K, V> of(K key1,
                               V value1,
                               K key2,
                               V value2) {
        requireNonNull(key1,
                       "key1");
        requireNonNull(value1,
                       "value1");
        requireNonNull(key2,
                       "key2");
        requireNonNull(value2,
                       "value2");
        return fromImmutableMap(HashMap.of(key1,
                                           value1,
                                           key2,
                                           value2));
    }

    static <K, V> Ent<K, V> ofSorted(K key,
                                     V value,
                                     Comparator<K> keyComparator) {
        requireNonNull(key,
                       "key");
        requireNonNull(value,
                       "value");
        requireNonNull(keyComparator,
                       "keyComparator");
        return fromImmutableMap(TreeMap.of(keyComparator,
                                           key,
                                           value));
    }

    static <K, V> Ent<K, V> ofSorted(K key1,
                                     V value1,
                                     K key2,
                                     V value2,
                                     Comparator<K> keyComparator) {
        requireNonNull(key1,
                       "key1");
        requireNonNull(value1,
                       "value1");
        requireNonNull(key2,
                       "key2");
        requireNonNull(value2,
                       "value2");
        requireNonNull(keyComparator,
                       "keyComparator");
        return fromImmutableMap(TreeMap.of(keyComparator,
                                           key1,
                                           value1,
                                           key2,
                                           value2));
    }

    static <K, V> Ent<K, V> ofTuple(Tuple2<K, V> tuple) {
        requireNonNull(tuple,
                       "tuple");
        return fromImmutableMap(HashMap.of(tuple._1(),
                                           tuple._2()));
    }

    static <K, V> Ent<K, V> ofTupleSorted(Tuple2<K, V> tuple,
                                          Comparator<K> keyComparator) {
        requireNonNull(tuple,
                       "tuple");
        requireNonNull(keyComparator,
                       "keyComparator");
        return fromImmutableMap(HashMap.of(tuple._1(),
                                           tuple._2()));
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

    static <K, V> Ent<K, V> fromSortedImmutableMap(ImmutableMap<K, V> immutableMap,
                                                   Comparator<K> keyComparator) {
        return Option.ofNullable(immutableMap)
                     .filter(iMap -> TreeMap.class.isAssignableFrom(iMap.getClass()))
                     .orElseUse(Option.ofNullable(immutableMap)
                                      .map(imap -> TreeMap.fromMap(keyComparator,
                                                                   imap)))
                     .fold(EntImpl::new,
                           () -> new EntImpl<>(TreeMap.empty(keyComparator)));
    }

    static <K, V> Ent<K, V> fromTuples(Iterable<Tuple2<K, V>> iterableOfTuples) {
        return Option.ofNullable(iterableOfTuples)
                     .map(iter -> HashMap.fromStream(Streamable.fromIterable(iter)))
                     .fold(EntImpl::new,
                           () -> Ent.empty());
    }

    static <K, V> Ent<K, V> fromSortedTuples(Iterable<Tuple2<K, V>> iterableOfTuples,
                                             Comparator<K> keyComparator) {
        return Option.ofNullable(iterableOfTuples)
                     .map(iter -> TreeMap.fromStream(Streamable.fromIterable(iter),
                                                     keyComparator))
                     .fold(EntImpl::new,
                           () -> Ent.emptySorted(keyComparator));
    }

    /**
     * Ent backing data structure
     */
    ImmutableMap<K, V> toImmutableMap();

    /**
     * Ent Specific Methods
     */

    default <VO> Option<VO> matchGetValue(K key,
                                          Pattern1<V, VO> valuePattern) {
        requireNonNull(key,
                       "key");
        requireNonNull(valuePattern,
                       "valuePattern");
        return toImmutableMap().get(key)
                               .map(Pattern1.asMapper(valuePattern));
    }

    default <KO, VO> Option<Tuple2<KO, VO>> matchGet(K key,
                                                     Pattern2<K, V, KO, VO> keyValuePattern) {
        requireNonNull(key,
                       "key");
        requireNonNull(keyValuePattern,
                       "keyValuePattern");
        return toImmutableMap().get(key)
                               .fold(v -> Option.of(Tuple2.of(key,
                                                              v))
                                                .map(Pattern2.asMapper(keyValuePattern)),
                                     Option::none);
    }

    default <VO> VO matchOrElseGetValue(K key,
                                        Pattern1<V, VO> valuePattern,
                                        Supplier<? extends VO> defaultValueSupplier) {
        requireNonNull(key,
                       "key");
        requireNonNull(valuePattern,
                       "valuePattern");
        requireNonNull(defaultValueSupplier,
                       "defaultValueSupplier");
        return toImmutableMap().get(key)
                               .map(Pattern1.asMapper(valuePattern))
                               .orElseGet(defaultValueSupplier);
    }

    default <KO, VO> Tuple2<KO, VO> matchOrElseGet(K key,
                                                   Pattern2<K, V, KO, VO> keyValuePattern,
                                                   Supplier<? extends Tuple2<KO, VO>> defaultKeyValueSupplier) {
        requireNonNull(key,
                       "key");
        requireNonNull(keyValuePattern,
                       "keyValuePattern");
        requireNonNull(defaultKeyValueSupplier,
                       "defaultKeyValueSupplier");
        return toImmutableMap().get(key)
                               .map(v -> Pattern2.asBiMapper(keyValuePattern)
                                                 .apply(key,
                                                        v))
                               .orElseGet(defaultKeyValueSupplier);
    }

    default <VO> Ent<K, VO> matchMapValues(Pattern1<V, VO> valuePattern) {
        requireNonNull(valuePattern,
                       "valuePattern");
        return fromImmutableMap(toImmutableMap().map(Pattern1.asMapper(valuePattern)));
    }

    default <KO> Ent<KO, V> matchFlatMapKeys(Pattern1<K, Ent<KO, V>> keyPattern) {
        requireNonNull(keyPattern,
                       "keyPattern");
        return fromImmutableMap(toImmutableMap().flatMap((k, v) -> Pattern1.asMapper(keyPattern)
                                                                           .apply(k)
                                                                           .toImmutableMap()));
    }

    default <VO> Ent<K, VO> matchFlatMapValues(Pattern1<V, Ent<K, VO>> valuePattern) {
        requireNonNull(valuePattern,
                       "valuePattern");
        return fromImmutableMap(toImmutableMap().flatMap((k, v) -> Pattern1.asMapper(valuePattern)
                                                                           .apply(v)
                                                                           .toImmutableMap()));
    }

    default <KO, VO> Ent<KO, VO> matchFlatMap(EntPattern2<K, V, KO, VO> entPattern) {
        requireNonNull(entPattern,
                       "entPattern");
        return fromImmutableMap(toImmutableMap().flatMap((k, v) -> EntPattern2.asBiMapper(entPattern)
                                                                              .apply(k,
                                                                                     v)
                                                                              .toImmutableMap()));
    }

    default <VO> ReactiveSeq<VO> matchMapValuesToReactiveSeq(Pattern1<V, VO> valuePattern) {
        requireNonNull(valuePattern,
                       "valuePattern");
        return toImmutableMap().map(Pattern1.asMapper(valuePattern))
                               .stream()
                               .map(Tuple2::_2);
    }

    default <VO> Stream<VO> matchMapValuesToStream(Pattern1<V, VO> valuePattern) {
        requireNonNull(valuePattern,
                       "valuePattern");
        return StreamSupport.stream(toImmutableMap().map(Pattern1.asMapper(valuePattern))
                                                    .stream()
                                                    .map(Tuple2::_2)
                                                    .spliterator(),
                                    PARALLEL);
    }

    default <KO, VO> Ent<KO, VO> matchMap(Pattern2<K, V, KO, VO> keyValuePattern) {
        requireNonNull(keyValuePattern,
                       "keyValuePattern");
        return fromImmutableMap(toImmutableMap().bimap(Pattern2.asBiMapper(keyValuePattern)));
    }

    default <KO, VO> ReactiveSeq<Tuple2<KO, VO>> matchMapToReactiveSeq(Pattern2<K, V, KO, VO> keyValuePattern) {
        requireNonNull(keyValuePattern,
                       "keyValuePattern");
        return toImmutableMap().bimap(Pattern2.asBiMapper(keyValuePattern))
                               .stream();
    }

    default <KO, VO> Stream<Tuple2<KO, VO>> matchMapToStream(Pattern2<K, V, KO, VO> keyValuePattern) {
        requireNonNull(keyValuePattern,
                       "keyValuePattern");
        return StreamSupport.stream(toImmutableMap().bimap(Pattern2.asBiMapper(keyValuePattern))
                                                    .stream()
                                                    .spliterator(),
                                    PARALLEL);
    }

    default <VO> Ent<K, VO> matchFilterValues(Pattern1<V, VO> valuePattern,
                                              Predicate<? super VO> predicate) {
        requireNonNull(valuePattern,
                       "valuePattern");
        requireNonNull(predicate,
                       "predicate");
        return fromImmutableMap(toImmutableMap().map(Pattern1.asMapper(valuePattern))
                                                .filter(kvoTuple2 -> predicate.test(kvoTuple2._2())));
    }

    default <KO> Ent<KO, V> matchFilterKeys(Pattern1<K, KO> keyPattern,
                                            Predicate<? super KO> predicate) {
        requireNonNull(keyPattern,
                       "keyPattern");
        requireNonNull(predicate,
                       "predicate");
        return fromImmutableMap(toImmutableMap().mapKeys(Pattern1.asMapper(keyPattern))
                                                .filter(kvoTuple2 -> predicate.test(kvoTuple2._1())));
    }

    default <KO, VO> Ent<KO, VO> matchFilter(Pattern2<K, V, KO, VO> keyValuePattern,
                                             Predicate<? super Tuple2<KO, VO>> predicate) {
        requireNonNull(keyValuePattern,
                       "keyValuePattern");
        requireNonNull(predicate,
                       "predicate");
        return fromImmutableMap(toImmutableMap().bimap(Pattern2.asBiMapper(keyValuePattern))
                                                .filter(predicate));
    }

    default <KO, VO> Ent<KO, VO> matchConcatMap(Pattern2<K, V, Iterable<KO>, Iterable<VO>> keyValuePattern) {
        requireNonNull(keyValuePattern,
                       "keyValuePattern");
        return fromImmutableMap(toImmutableMap().concatMap(Pattern2.asConcatMapper(keyValuePattern)));
    }

    default <KO, VO, R> R matchFoldMap(R zero,
                                       Pattern2<K, V, KO, VO> keyValuePattern,
                                       Function<Tuple2<KO, VO>, R> mapper,
                                       BinaryOperator<R> combiner) {
        requireNonNull(zero,
                       "zero");
        requireNonNull(keyValuePattern,
                       "keyValuePattern");
        requireNonNull(mapper,
                       "mapper");
        requireNonNull(combiner,
                       "combiner");
        return toImmutableMap().foldMap(Reducer.of(zero,
                                                   combiner,
                                                   tuple2 -> mapper.apply(Pattern2.asMapper(keyValuePattern)
                                                                                  .apply(tuple2))));
    }

    default <R> R matchFoldMapKeys(R zero,
                                   Pattern1<K, R> keyPattern,
                                   BinaryOperator<R> combiner) {
        requireNonNull(zero,
                       "zero");
        requireNonNull(keyPattern,
                       "valuePattern");
        requireNonNull(combiner,
                       "combiner");
        return toImmutableMap().foldMap(Reducer.of(zero,
                                                   combiner,
                                                   tuple2 -> Pattern1.asMapper(keyPattern)
                                                                     .apply(tuple2._1())));
    }

    default <R> R matchFoldMapValues(R zero,
                                     Pattern1<V, R> valuePattern,
                                     BinaryOperator<R> combiner) {
        requireNonNull(zero,
                       "zero");
        requireNonNull(valuePattern,
                       "valuePattern");
        requireNonNull(combiner,
                       "combiner");
        return toImmutableMap().foldMap(Reducer.of(zero,
                                                   combiner,
                                                   tuple2 -> Pattern1.asMapper(valuePattern)
                                                                     .apply(tuple2._2())));
    }

    default <VI> Ent<K, V> matchPut(K key,
                                    VI inputValue,
                                    Pattern1<VI, V> inputValuePattern) {
        requireNonNull(key,
                       "key");
        requireNonNull(inputValue,
                       "inputValue");
        requireNonNull(inputValuePattern,
                       "inputValuePattern");
        return Option.of(inputValue)
                     .map(Pattern1.asMapper(inputValuePattern))
                     .fold(v -> fromImmutableMap(toImmutableMap().put(key,
                                                                      v)),
                           () -> this);
    }

    default <KI, VI> Ent<K, V> matchPut(KI inputKey,
                                        VI inputValue,
                                        Pattern2<KI, VI, K, V> inputKeyValuePattern) {
        requireNonNull(inputKey,
                       "inputKey");
        requireNonNull(inputValue,
                       "inputValue");
        requireNonNull(inputKeyValuePattern,
                       "inputKeyValuePattern");
        return Option.of(Tuple2.of(inputKey,
                                   inputValue))
                     .map(Pattern2.asMapper(inputKeyValuePattern))
                     .fold(tuple -> fromImmutableMap(toImmutableMap().put(tuple)),
                           () -> this);
    }

    default <KI, VI> Ent<K, V> matchPutAll(Iterable<? extends Tuple2<KI, VI>> inputKeyValueTuples,
                                           Pattern2<KI, VI, K, V> inputKeyValuePattern) {
        requireNonNull(inputKeyValueTuples,
                       "inputKeyValueTuples");
        requireNonNull(inputKeyValuePattern,
                       "inputKeyValuePattern");
        return fromImmutableMap(Streamable.fromIterable(inputKeyValueTuples)
                                          .map(tuple -> Option.ofNullable(tuple)
                                                              .map(Pattern2.asMapper(inputKeyValuePattern)))
                                          .filter(Option::isPresent)
                                          .map(tupOpt -> tupOpt.orElse(null))
                                          .foldRight(toImmutableMap(),
                                                     (tuple, immutMap) -> immutMap.put(tuple)));
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

    default <R> Ent<K, R> map(Function<? super V, ? extends R> mapper) {
        requireNonNull(mapper,
                       "mapper");
        return fromImmutableMap(toImmutableMap().map(mapper));
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
     * Monadic Methods: Streams
     */

    default ReactiveSeq<Tuple2<K, V>> stream() {
        return toImmutableMap().stream();
    }

    default <R> R collect(Supplier<R> supplier,
                          BiConsumer<R, ? super Tuple2<K, V>> accumulator,
                          BiConsumer<R, R> combiner) {
        requireNonNull(supplier,
                       "supplier");
        requireNonNull(accumulator,
                       "accumulator");
        requireNonNull(combiner,
                       "combiner");
        return toImmutableMap().collect(supplier,
                                        accumulator,
                                        combiner);
    }

    default <KO, VO, A1, A2> Tuple2<KO, VO> collect(Collector<? super Tuple2<K, V>, A1, KO> keyCollector,
                                                    Collector<? super Tuple2<K, V>, A2, VO> valueCollector) {
        requireNonNull(keyCollector,
                       "keyCollector");
        requireNonNull(valueCollector,
                       "valueCollector");
        return toImmutableMap().collect(keyCollector,
                                        valueCollector);
    }

    default <R1, R2, R3, A1, A2, A3> Tuple3<R1, R2, R3> collect(Collector<? super Tuple2<K, V>, A1, R1> firstCollector,
                                                                Collector<? super Tuple2<K, V>, A2, R2> secondCollector,
                                                                Collector<? super Tuple2<K, V>, A3, R3> thirdCollector) {
        requireNonNull(firstCollector,
                       "firstCollector");
        requireNonNull(secondCollector,
                       "secondCollector");
        requireNonNull(thirdCollector,
                       "thirdCollector");
        return toImmutableMap().collect(firstCollector,
                                        secondCollector,
                                        thirdCollector);
    }

    default <R, A> R collect(Collector<? super Tuple2<K, V>, A, R> collector) {
        requireNonNull(collector,
                       "collector");
        return toImmutableMap().collect(collector);
    }


    default <KO> HashMap<KO, Vector<Tuple2<K, V>>> groupBy(Function<? super Tuple2<K, V>, ? extends KO> classifier) {
        requireNonNull(classifier,
                       "classifier");
        return toImmutableMap().groupBy(classifier);
    }


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

    default <VO extends V> Ent<K, V> filterValuesOfType(Class<? extends VO> valueSubType) {
        requireNonNull(valueSubType,
                       "possibleValueType");
        return fromImmutableMap(toImmutableMap().filterValues(VariantMapper.inputTypeFilter(valueSubType)));
    }

    /**
     * Monadic Methods: Debuggers
     */

    default Ent<K, V> peek(Consumer<? super V> peeker) {
        requireNonNull(peeker,
                       "peeker");
        return fromImmutableMap(toImmutableMap().peek(peeker));
    }

    default Ent<K, V> bipeek(Consumer<? super K> keyPeeker,
                             Consumer<? super V> valuePeeker) {
        requireNonNull(keyPeeker,
                       "keyPeeker");
        requireNonNull(valuePeeker,
                       "valuePeeker");
        return fromImmutableMap(toImmutableMap().bipeek(keyPeeker,
                                                        valuePeeker));
    }

    default Ent<K, V> bipeek(BiConsumer<? super K, ? super V> biPeeker) {
        requireNonNull(biPeeker,
                       "biPeeker");
        return fromImmutableMap(toImmutableMap().bimap((k, v) -> {
            biPeeker.accept(k,
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

    default Ent<K, V> onEmptyGet(Supplier<? extends Tuple2<K, V>> keyValueSuppler) {
        requireNonNull(keyValueSuppler,
                       "keyValueSupplier");
        return fromImmutableMap(toImmutableMap().onEmptyGet(keyValueSuppler));
    }

    default <X extends Throwable> Try<Ent<K, V>, X> onEmptyTry(Supplier<? extends X> throwableSupplier) {
        requireNonNull(throwableSupplier,
                       "throwableSupplier");
        return isEmpty() ? Try.failure(throwableSupplier.get()) : Try.success(this);
    }

    default Ent<K, V> onEmptySwitch(Supplier<? extends Ent<K, V>> entSupplier) {
        requireNonNull(entSupplier,
                       "entSupplier");
        return isEmpty() ? entSupplier.get() : this;
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
                        V defaultValue) {
        requireNonNull(key,
                       "key");
        // defaultValue is permitted to be null
        return toImmutableMap().getOrElse(key,
                                          defaultValue);
    }

    default V getOrElseGet(K key,
                           Supplier<? extends V> defaultValueSupplier) {
        requireNonNull(key,
                       "key");
        requireNonNull(defaultValueSupplier,
                       "defaultValueSupplier");
        return toImmutableMap().getOrElseGet(key,
                                             defaultValueSupplier);
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

    default Ent<K, V> putAll(Map<? extends K, ? extends V> map) {
        requireNonNull(map,
                       "map");
        return fromImmutableMap(ReactiveSeq.fromIterable(map.entrySet())
                                           .foldRight(toImmutableMap(),
                                                      (entry, immMap) -> immMap.put(entry.getKey(),
                                                                                    entry.getValue())));
    }

    default Ent<K, V> remove(K key) {
        requireNonNull(key,
                       "key");
        return fromImmutableMap(toImmutableMap().remove(key));
    }

    default Ent<K, V> removeAll(Iterable<? extends K> keys) {
        requireNonNull(keys,
                       "keys");
        return fromImmutableMap(toImmutableMap().removeAllKeys(keys));
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

    default boolean contains(Tuple2<K, V> keyValueTuple) {
        requireNonNull(keyValueTuple,
                       "keyValueTuple");
        return toImmutableMap().contains(keyValueTuple);
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

    default boolean startsWith(Iterable<Tuple2<K, V>> iterable) {
        requireNonNull(iterable,
                       "iterable");
        return toImmutableMap().startsWith(iterable);
    }

    default boolean endsWith(Iterable<Tuple2<K, V>> iterable) {
        requireNonNull(iterable,
                       "iterable");
        return toImmutableMap().endsWith(iterable);
    }

    /**
     * Fancy ForEach Comprehensions
     */

    default <K1, K2, K3, K4, V1, V2, V3, VO> Ent<K4, VO> forEach4(Function<? super Tuple2<K, V>, ? extends Iterable<Tuple2<K1, V1>>> keyValueMapConcatenator1,
                                                                  BiFunction<? super Tuple2<K, V>, ? super Tuple2<K1, V1>, ? extends Iterable<Tuple2<K2, V2>>> keyValueMapConcatenator2,
                                                                  Function3<? super Tuple2<K, V>, ? super Tuple2<K1, V1>, ? super Tuple2<K2, V2>, ? extends Iterable<Tuple2<K3, V3>>> keyValueMapConcatenator3,
                                                                  Function4<? super Tuple2<K, V>, ? super Tuple2<K1, V1>, ? super Tuple2<K2, V2>, ? super Tuple2<K3, V3>, ? extends Tuple2<K4, VO>> yieldingFunction) {

        return fromImmutableMap(toImmutableMap().forEach4(keyValueMapConcatenator1,
                                                          keyValueMapConcatenator2,
                                                          keyValueMapConcatenator3,
                                                          yieldingFunction));
    }

    default <K1, K2, K3, K4, V1, V2, V3, VO> Ent<K4, VO> forEach4(Function<? super Tuple2<K, V>, ? extends Iterable<Tuple2<K1, V1>>> keyValueMapConcatenator1,
                                                                  BiFunction<? super Tuple2<K, V>, ? super Tuple2<K1, V1>, ? extends Iterable<Tuple2<K2, V2>>> keyValueMapConcatenator2,
                                                                  Function3<? super Tuple2<K, V>, ? super Tuple2<K1, V1>, ? super Tuple2<K2, V2>, ? extends Iterable<Tuple2<K3, V3>>> keyValueMapConcatenator3,
                                                                  Function4<? super Tuple2<K, V>, ? super Tuple2<K1, V1>, ? super Tuple2<K2, V2>, ? super Tuple2<K3, V3>, Boolean> filter,
                                                                  Function4<? super Tuple2<K, V>, ? super Tuple2<K1, V1>, ? super Tuple2<K2, V2>, ? super Tuple2<K3, V3>, ? extends Tuple2<K4, VO>> yieldingFunction) {

        requireNonNull(keyValueMapConcatenator1,
                       "keyValueMapConcatenator1");
        requireNonNull(keyValueMapConcatenator2,
                       "keyValueMapConcatenator2");
        requireNonNull(keyValueMapConcatenator3,
                       "keyValueMapConcatenator3");
        requireNonNull(filter,
                       "filter");
        requireNonNull(yieldingFunction,
                       "yieldingFunction");
        return fromImmutableMap(toImmutableMap().forEach4(keyValueMapConcatenator1,
                                                          keyValueMapConcatenator2,
                                                          keyValueMapConcatenator3,
                                                          filter,
                                                          yieldingFunction));
    }

    default <K1, K2, K3, V1, V2, VO> Ent<K3, VO> forEach3(Function<? super Tuple2<K, V>, ? extends Iterable<Tuple2<K1, V1>>> keyValueMapConcatenator1,
                                                          BiFunction<? super Tuple2<K, V>, ? super Tuple2<K1, V1>, ? extends Iterable<Tuple2<K2, V2>>> keyValueMapConcatenator2,
                                                          Function3<? super Tuple2<K, V>, ? super Tuple2<K1, V1>, ? super Tuple2<K2, V2>, ? extends Tuple2<K3, VO>> yieldingFunction) {
        requireNonNull(keyValueMapConcatenator1,
                       "keyValueMapConcatenator1");
        requireNonNull(keyValueMapConcatenator2,
                       "keyValueMapConcatenator2");
        requireNonNull(yieldingFunction,
                       "yieldingFunction");

        return fromImmutableMap(toImmutableMap().forEach3(keyValueMapConcatenator1,
                                                          keyValueMapConcatenator2,
                                                          yieldingFunction));
    }

    default <K1, K2, K3, V1, V2, VO> Ent<K3, VO> forEach3(Function<? super Tuple2<K, V>, ? extends Iterable<Tuple2<K1, V1>>> keyValueMapConcatenator1,
                                                          BiFunction<? super Tuple2<K, V>, ? super Tuple2<K1, V1>, ? extends Iterable<Tuple2<K2, V2>>> keyValueMapConcatenator2,
                                                          Function3<? super Tuple2<K, V>, ? super Tuple2<K1, V1>, ? super Tuple2<K2, V2>, Boolean> filter,
                                                          Function3<? super Tuple2<K, V>, ? super Tuple2<K1, V1>, ? super Tuple2<K2, V2>, ? extends Tuple2<K3, VO>> yieldingFunction) {
        requireNonNull(keyValueMapConcatenator1,
                       "keyValueMapConcatenator1");
        requireNonNull(keyValueMapConcatenator2,
                       "keyValueMapConcatenator2");
        requireNonNull(filter,
                       "filter");
        requireNonNull(yieldingFunction,
                       "yieldingFunction");

        return fromImmutableMap(toImmutableMap().forEach3(keyValueMapConcatenator1,
                                                          keyValueMapConcatenator2,
                                                          filter,
                                                          yieldingFunction));
    }

    default <KI, VI, KO, VO> Ent<KO, VO> forEach2(Function<? super Tuple2<K, V>, ? extends Iterable<Tuple2<KI, VI>>> keyValueMapConcatenator,
                                                  BiFunction<? super Tuple2<K, V>, ? super Tuple2<KI, VI>, ? extends Tuple2<KO, VO>> yieldingFunction) {
        requireNonNull(keyValueMapConcatenator,
                       "keyValueMapConcatenator");
        requireNonNull(yieldingFunction,
                       "yieldingFunction");
        return fromImmutableMap(toImmutableMap().forEach2(keyValueMapConcatenator,
                                                          yieldingFunction));
    }

    default <KI, VI, KO, VO> Ent<KO, VO> forEach2(Function<? super Tuple2<K, V>, ? extends Iterable<Tuple2<KI, VI>>> keyValueMapConcatenator,
                                                  BiPredicate<? super Tuple2<K, V>, ? super Tuple2<KI, VI>> filter,
                                                  BiFunction<? super Tuple2<K, V>, ? super Tuple2<KI, VI>, ? extends Tuple2<KO, VO>> yieldingFunction) {
        requireNonNull(keyValueMapConcatenator,
                       "keyValueMapConcatenator");
        requireNonNull(filter,
                       "filter");
        requireNonNull(yieldingFunction,
                       "yieldingFunction");
        return fromImmutableMap(toImmutableMap().forEach2(keyValueMapConcatenator,
                                                          filter::test,
                                                          yieldingFunction));
    }

    /**
     * Transformers
     */

    default Map<K, V> toJavaMap() {
        return toImmutableMap().collect(Collectors.toMap(Tuple2::_1,
                                                         Tuple2::_2));
    }


    default <KO, VO> HashMap<KO, VO> toHashMap(Function<? super Tuple2<K, V>, ? extends KO> keyMapper,
                                               Function<? super Tuple2<K, V>, ? extends VO> valueMapper) {
        requireNonNull(keyMapper,
                       "keyMapper");
        requireNonNull(valueMapper,
                       "valueMapper");
        return toImmutableMap().toHashMap(keyMapper,
                                          valueMapper);
    }

    default <KO, VO> Map<KO, VO> toJavaMap(Function<? super Tuple2<K, V>, ? extends KO> keyMapper,
                                           Function<? super Tuple2<K, V>, ? extends VO> valueMapper) {
        requireNonNull(keyMapper,
                       "keyMapper");
        requireNonNull(valueMapper,
                       "valueMapper");
        return toImmutableMap().toMap(keyMapper,
                                      valueMapper);
    }

    default <KO> HashMap<KO, Tuple2<K, V>> toHashMap(Function<? super Tuple2<K, V>, ? extends KO> keyMapper) {
        requireNonNull(keyMapper,
                       "keyMapper");
        return toImmutableMap().toHashMap(keyMapper);
    }

    default <KO> Map<KO, Tuple2<K, V>> toMap(Function<? super Tuple2<K, V>, ? extends KO> keyMapper) {
        requireNonNull(keyMapper,
                       "keyMapper");
        return toImmutableMap().toMap(keyMapper);
    }

    default <T> Seq<T> toSeq(Function<? super Tuple2<? super K, ? super V>, ? extends T> keyValueMapper) {
        requireNonNull(keyValueMapper,
                       "keyValueMapper");
        return toImmutableMap().toSeq(keyValueMapper);
    }

    default <T> LazySeq<T> toLazySeq(Function<? super Tuple2<? super K, ? super V>, ? extends T> keyValueMapper) {
        requireNonNull(keyValueMapper,
                       "keyValueMapper");
        return toImmutableMap().toLazySeq(keyValueMapper);
    }

    default <R> R[] toArray(IntFunction<R[]> generator) {
        requireNonNull(generator,
                       "generator");
        return toImmutableMap().toArray(generator);
    }

    default Object[] toArray() {
        return toImmutableMap().toArray();
    }

    default <R> R to(Function<? super Iterable<? super Tuple2<K, V>>, ? extends R> iterableMapper) {
        requireNonNull(iterableMapper,
                       "iterableMapper");
        return iterableMapper.apply(toImmutableMap());
    }

    default BankersQueue<Tuple2<K, V>> bankersQueue() {
        return toImmutableMap().bankersQueue();
    }

    default TreeSet<Tuple2<K, V>> treeSet(Comparator<? super Tuple2<K, V>> keyValueComparator) {
        requireNonNull(keyValueComparator,
                       "keyValueComparator");
        return toImmutableMap().treeSet(keyValueComparator);
    }

    default HashSet<Tuple2<K, V>> hashSet() {
        return toImmutableMap().hashSet();
    }

    default Vector<Tuple2<K, V>> vector() {
        return toImmutableMap().vector();
    }

    default LazySeq<Tuple2<K, V>> lazySeq() {
        return toImmutableMap().lazySeq();
    }

    default Seq<Tuple2<K, V>> seq() {
        return toImmutableMap().seq();
    }

    default NonEmptyList<Tuple2<K, V>> nonEmptyList(Supplier<Tuple2<K, V>> defaultKeyValueSupplier) {
        requireNonNull(defaultKeyValueSupplier,
                       "defaultKeyValueSupplier");
        return toImmutableMap().nonEmptyList(defaultKeyValueSupplier);
    }

    default <T extends Collection<Tuple2<K, V>>> T toCollection(Supplier<T> collectionFactory) {
        requireNonNull(collectionFactory,
                       "collectionFactory");
        return toImmutableMap().toCollection(collectionFactory);
    }

    default List<Tuple2<K, V>> toList() {
        return toImmutableMap().toList();
    }

    default Set<Tuple2<K, V>> toSet() {
        return toImmutableMap().toSet();
    }

    /**
     * Stats Methods
     */

    default long count() {
        return toImmutableMap().count();
    }

    default long countDistinct() {
        return toImmutableMap().countDistinct();
    }

    default <U> Option<Tuple2<K, V>> maxBy(Function<? super Tuple2<K, V>, ? extends U> extractor,
                                           Comparator<? super U> comparator) {
        requireNonNull(extractor,
                       "extractor");
        requireNonNull(comparator,
                       "comparator");
        return toImmutableMap().maxBy(extractor,
                                      comparator);
    }

    default <U extends Comparable<? super U>> Option<Tuple2<K, V>> maxBy(Function<? super Tuple2<K, V>, ? extends U> extractor) {
        requireNonNull(extractor,
                       "extractor");
        return toImmutableMap().maxBy(extractor);
    }

    default <U extends Comparable<? super U>> Option<Tuple2<K, V>> minBy(Function<? super Tuple2<K, V>, ? extends U> extractor) {
        requireNonNull(extractor,
                       "extractor");
        return toImmutableMap().minBy(extractor);
    }

    default <U extends Comparable<? super U>> Option<Tuple2<K, V>> minBy(Function<? super Tuple2<K, V>, ? extends U> extractor,
                                                                         Comparator<? super U> comparator) {
        requireNonNull(extractor,
                       "extractor");
        requireNonNull(comparator,
                       "comparator");
        return toImmutableMap().minBy(extractor,
                                      comparator);
    }

    default Option<Tuple2<K, V>> maximum(Comparator<? super Tuple2<K, V>> keyValueComparator) {
        requireNonNull(keyValueComparator,
                       "keyValueComparator");
        return toImmutableMap().maximum(keyValueComparator);

    }

    default Option<Tuple2<K, V>> minimum(Comparator<? super Tuple2<K, V>> keyValueComparator) {
        requireNonNull(keyValueComparator,
                       "keyValueComparator");
        return toImmutableMap().minimum(keyValueComparator);
    }

    default Option<Tuple2<K, V>> mode() {
        return toImmutableMap().mode();
    }

    default ReactiveSeq<Tuple2<Tuple2<K, V>, Integer>> occurrences() {
        return toImmutableMap().occurances();
    }

    default double mean(ToDoubleFunction<Tuple2<K, V>> mapper) {
        requireNonNull(mapper,
                       "mapper");
        return toImmutableMap().mean(mapper);
    }

    default Option<Tuple2<K, V>> median() {
        return toImmutableMap().median();
    }

    default Seq<Tuple2<Tuple2<K, V>, BigDecimal>> withPercentiles() {
        return toImmutableMap().withPercentiles();
    }

    default Option<Tuple2<K, V>> atPercentile(double percentile) {
        return toImmutableMap().atPercentile(percentile);
    }

    default double variance(ToDoubleFunction<Tuple2<K, V>> mapper) {
        requireNonNull(mapper,
                       "mapper");
        return toImmutableMap().variance(mapper);
    }

    default double populationVariance(ToDoubleFunction<Tuple2<K, V>> mapper) {
        requireNonNull(mapper,
                       "mapper");
        return toImmutableMap().populationVariance(mapper);
    }

    default double stdDeviation(ToDoubleFunction<Tuple2<K, V>> mapper) {
        requireNonNull(mapper,
                       "mapper");
        return toImmutableMap().stdDeviation(mapper);
    }


    default int sumInt(ToIntFunction<Tuple2<K, V>> mapper) {
        requireNonNull(mapper,
                       "mapper");
        return toImmutableMap().sumInt(mapper);
    }

    default double sumDouble(ToDoubleFunction<Tuple2<K, V>> mapper) {
        requireNonNull(mapper,
                       "mapper");
        return toImmutableMap().sumDouble(mapper);
    }

    default long sumLong(ToLongFunction<Tuple2<K, V>> mapper) {
        requireNonNull(mapper,
                       "mapper");
        return toImmutableMap().sumLong(mapper);
    }

    /**
     * Sampling Methods
     */

    default Option<Tuple2<K, V>> headOption() {
        return toImmutableMap().headOption();
    }

    default Option<Tuple2<K, V>> tailOption() {
        return toImmutableMap().stream()
                               .takeRight(1)
                               .headOption();
    }

    default Tuple2<K, V> firstValue(Tuple2<K, V> defaultKeyValue) {
        // defaultKeyValue is allowed to be null
        return toImmutableMap().firstValue(defaultKeyValue);
    }

    default Tuple2<K, V> singleOrElse(Tuple2<K, V> defaultKeyValue) {
        // defaultKeyValue is allowed to be null
        return toImmutableMap().singleOrElse(defaultKeyValue);
    }

    default Maybe<Tuple2<K, V>> single(Predicate<? super Tuple2<K, V>> condition) {
        requireNonNull(condition,
                       "condition");
        return toImmutableMap().single(condition);
    }

    default Maybe<Tuple2<K, V>> single() {
        return toImmutableMap().single();
    }

    default Maybe<Tuple2<K, V>> takeOne() {
        return toImmutableMap().takeOne();
    }

    default Maybe<Tuple2<K, V>> elementAt(long index) {
        return toImmutableMap().elementAt(index);
    }

    default Maybe<Long> indexOf(Predicate<? super Tuple2<K, V>> condition) {
        requireNonNull(condition,
                       "condition");
        return toImmutableMap().indexOf(condition);
    }

    default Maybe<Long> lastIndexOf(Predicate<? super Tuple2<K, V>> condition) {
        requireNonNull(condition,
                       "condition");
        return toImmutableMap().lastIndexOf(condition);
    }

    default Maybe<Long> indexOfSlice(Iterable<? extends Tuple2<K, V>> slice) {
        requireNonNull(slice,
                       "slice");
        return toImmutableMap().indexOfSlice(slice);
    }

    default Maybe<Long> lastIndexOfSlice(Iterable<? extends Tuple2<K, V>> slice) {
        requireNonNull(slice,
                       "slice");
        return toImmutableMap().lastIndexOfSlice(slice);
    }

    default boolean atLeast(int num,
                            Predicate<? super Tuple2<K, V>> condition) {
        requireNonNull(condition,
                       "condition");
        return toImmutableMap().atLeast(num,
                                        condition);
    }

    default boolean atMost(int num,
                           Predicate<? super Tuple2<K, V>> condition) {
        requireNonNull(condition,
                       "condition");
        return toImmutableMap().atMost(num,
                                       condition);
    }

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
