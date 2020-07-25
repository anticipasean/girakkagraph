package io.github.anticipasean.ent.state;

import cyclops.data.HashMap;
import cyclops.data.ImmutableMap;
import cyclops.data.tuple.Tuple2;
import io.github.anticipasean.ent.Ent;
import java.util.Collections;
import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class EmptyEnt<K, V> implements Ent<K, V> {


    @SuppressWarnings("unchecked")
    public static <K, V> Ent<K, V> emptyEnt() {
        return (Ent<K, V>) EmptyEntSupplier.INSTANCE.get();
    }

    @Override
    public ImmutableMap<K, V> toImmutableMap() {
        return HashMap.empty();
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public <R> Ent<K, R> map(Function<? super V, ? extends R> fn) {
        return emptyEnt();
    }

    @Override
    public <R> Ent<K, R> mapValues(Function<? super V, ? extends R> mapper) {
        return emptyEnt();
    }

    @Override
    public <R> Ent<R, V> mapKeys(Function<? super K, ? extends R> mapper) {
        return emptyEnt();
    }

    @Override
    public <R1, R2> Ent<R1, R2> bimap(BiFunction<? super K, ? super V, ? extends Tuple2<R1, R2>> map) {
        return emptyEnt();
    }

    @Override
    public <R1, R2> Ent<R1, R2> bimap(Function<? super K, ? extends R1> fn1,
                                      Function<? super V, ? extends R2> fn2) {
        return emptyEnt();
    }

    @Override
    public <K2, V2> Ent<K2, V2> flatMap(BiFunction<? super K, ? super V, ? extends ImmutableMap<K2, V2>> mapper) {
        return emptyEnt();
    }

    @Override
    public <K2, V2> Ent<K2, V2> concatMap(BiFunction<? super K, ? super V, ? extends Iterable<Tuple2<K2, V2>>> mapper) {
        return emptyEnt();
    }
//
//    @Override
//    public <K2, V2> Two<K, V, K2, V2> merge(ImmutableMap<K2, V2> one) {
//        return null;
//    }
//
//    @Override
//    public <K2, V2, K3, V3> Three<K, V, K2, V2, K3, V3> merge(Two<K2, V2, K3, V3> two) {
//        return null;
//    }
//
//    @Override
//    public ReactiveSeq<Tuple2<K, V>> stream() {
//        return null;
//    }
//
//    @Override
//    public <R> R collect(Supplier<R> supplier,
//                         BiConsumer<R, ? super Tuple2<K, V>> accumulator,
//                         BiConsumer<R, R> combiner) {
//        return null;
//    }
//
//    @Override
//    public <R1, R2, A1, A2> Tuple2<R1, R2> collect(Collector<? super Tuple2<K, V>, A1, R1> c1,
//                                                   Collector<? super Tuple2<K, V>, A2, R2> c2) {
//        return null;
//    }
//
//    @Override
//    public <R1, R2, R3, A1, A2, A3> Tuple3<R1, R2, R3> collect(Collector<? super Tuple2<K, V>, A1, R1> c1,
//                                                               Collector<? super Tuple2<K, V>, A2, R2> c2,
//                                                               Collector<? super Tuple2<K, V>, A3, R3> c3) {
//        return null;
//    }
//
    @Override
    public Ent<K, V> filter(Predicate<? super Tuple2<K, V>> predicate) {
        return emptyEnt();
    }

    @Override
    public Ent<K, V> filterKeys(Predicate<? super K> predicate) {
        return emptyEnt();
    }

    @Override
    public Ent<K, V> filterValues(Predicate<? super V> predicate) {
        return emptyEnt();
    }

    @Override
    public Ent<K, V> filterNot(Predicate<? super Tuple2<K, V>> predicate) {
        return emptyEnt();
    }

    @Override
    public Ent<K, V> notNull() {
        return emptyEnt();
    }

    @Override
    public Ent<K, V> peek(Consumer<? super V> c) {
        return emptyEnt();
    }

    @Override
    public Ent<K, V> bipeek(Consumer<? super K> c1,
                             Consumer<? super V> c2) {
        return emptyEnt();
    }

    @Override
    public Ent<K, V> bipeek(BiConsumer<? super K, ? super V> peeker) {
        return emptyEnt();
    }

    @Override
    public String mkString() {
        return "[]";
    }

    @Override
    public String join() {
        return "";
    }

    @Override
    public String join(String sep) {
        return "";
    }

    @Override
    public String join(String sep,
                       String start,
                       String end) {
        return "";
    }
//
//    @Override
//    public Ent<K, V> onEmpty(Tuple2<K, V> value) {
//        return null;
//    }
//
//    @Override
//    public Ent<K, V> onEmptyGet(Supplier<? extends Tuple2<K, V>> supplier) {
//        return null;
//    }
//
//    @Override
//    public <X extends Throwable> Try<Ent<K, V>, X> onEmptyTry(Supplier<? extends X> supplier) {
//        return null;
//    }
//
//    @Override
//    public Ent<K, V> onEmptySwitch(Supplier<? extends Ent<K, V>> supplier) {
//        return null;
//    }
//
//    @Override
//    public <R> R foldMap(Reducer<R, Tuple2<K, V>> reducer) {
//        return null;
//    }
//
//    @Override
//    public <R> R foldMap(Function<? super Tuple2<K, V>, ? extends R> mapper,
//                         Monoid<R> reducer) {
//        return null;
//    }
//
//    @Override
//    public Option<Tuple2<K, V>> foldLeft(BinaryOperator<Tuple2<K, V>> accumulator) {
//        return null;
//    }
//
//    @Override
//    public <U> U foldLeft(U identity,
//                          BiFunction<U, ? super Tuple2<K, V>, U> accumulator) {
//        return null;
//    }
//
//    @Override
//    public <U> U foldLeft(U identity,
//                          BiFunction<U, ? super Tuple2<K, V>, U> accumulator,
//                          BinaryOperator<U> combiner) {
//        return null;
//    }
//
//    @Override
//    public Tuple2<K, V> foldLeft(Tuple2<K, V> identity,
//                                  BinaryOperator<Tuple2<K, V>> accumulator) {
//        return null;
//    }
//
//    @Override
//    public Tuple2<K, V> foldLeft(Monoid<Tuple2<K, V>> reducer) {
//        return null;
//    }
//
//    @Override
//    public Seq<Tuple2<K, V>> foldLeft(Iterable<? extends Monoid<Tuple2<K, V>>> reducers) {
//        return null;
//    }
//
//    @Override
//    public Tuple2<K, V> foldRight(Monoid<Tuple2<K, V>> reducer) {
//        return null;
//    }
//
//    @Override
//    public Tuple2<K, V> foldRight(Tuple2<K, V> identity,
//                                   BinaryOperator<Tuple2<K, V>> accumulator) {
//        return null;
//    }
//
//    @Override
//    public <U> U foldRight(U identity,
//                           BiFunction<? super Tuple2<K, V>, ? super U, ? extends U> accumulator) {
//        return null;
//    }
//
//    @Override
//    public <R> R foldMapRight(Reducer<R, Tuple2<K, V>> reducer) {
//        return null;
//    }
//
//    @Override
//    public Option<V> get(K key) {
//        return Option.none();
//    }
//
//    @Override
//    public V getOrElse(K key,
//                       V alt) {
//        return alt;
//    }
//
//    @Override
//    public V getOrElseGet(K key,
//                          Supplier<? extends V> alt) {
//        return alt.get();
//    }
//
//    @Override
//    public Ent<K, V> put(K key,
//                          V value) {
//        return new FilledEnt<>(HashMap.of(key, value));
//    }
//
//    @Override
//    public Ent<K, V> put(Tuple2<K, V> keyAndValue) {
//        return new FilledEnt<>(HashMap.of(keyAndValue._1(), keyAndValue._2()));
//    }
//
//    @Override
//    public Ent<K, V> putAll(PersistentMap<? extends K, ? extends V> map) {
//        return null;
//    }
//
//    @Override
//    public Ent<K, V> remove(K key) {
//        return emptyEnt();
//    }
//
//    @Override
//    public Ent<K, V> removeAll(K... keys) {
//        return emptyEnt();
//    }
//
//    @Override
//    public ReactiveSeq<K> keys() {
//        return null;
//    }
//
//    @Override
//    public ReactiveSeq<V> values() {
//        return null;
//    }
//
//    @Override
//    public Ent<K, V> removeAllKeys(Iterable<? extends K> keys) {
//        return emptyEnt();
//    }
//
//    @Override
//    public boolean containsValue(V value) {
//        return false;
//    }
//
//    @Override
//    public boolean isEmpty() {
//        return true;
//    }
//
//    @Override
//    public boolean containsKey(K key) {
//        return false;
//    }
//
//    @Override
//    public boolean contains(Tuple2<K, V> t) {
//        return false;
//    }
//
//    @Override
//    public boolean allMatch(Predicate<? super Tuple2<K, V>> c) {
//        return false;
//    }
//
//    @Override
//    public boolean anyMatch(Predicate<? super Tuple2<K, V>> c) {
//        return false;
//    }
//
//    @Override
//    public boolean noneMatch(Predicate<? super Tuple2<K, V>> c) {
//        return false;
//    }
//
//    @Override
//    public <K1, K2, K3, K4, R1, R2, R3, R> ImmutableMap<K4, R> forEach4(Function<? super Tuple2<K, V>, ? extends Iterable<Tuple2<K1, R1>>> iterable1,
//                                                                        BiFunction<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? extends Iterable<Tuple2<K2, R2>>> iterable2,
//                                                                        Function3<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? super Tuple2<K2, R2>, ? extends Iterable<Tuple2<K3, R3>>> iterable3,
//                                                                        Function4<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? super Tuple2<K2, R2>, ? super Tuple2<K3, R3>, ? extends Tuple2<K4, R>> yieldingFunction) {
//        return null;
//    }
//
//    @Override
//    public <K1, K2, K3, K4, R1, R2, R3, R> ImmutableMap<K4, R> forEach4(Function<? super Tuple2<K, V>, ? extends Iterable<Tuple2<K1, R1>>> iterable1,
//                                                                        BiFunction<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? extends Iterable<Tuple2<K2, R2>>> iterable2,
//                                                                        Function3<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? super Tuple2<K2, R2>, ? extends Iterable<Tuple2<K3, R3>>> iterable3,
//                                                                        Function4<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? super Tuple2<K2, R2>, ? super Tuple2<K3, R3>, Boolean> filterFunction,
//                                                                        Function4<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? super Tuple2<K2, R2>, ? super Tuple2<K3, R3>, ? extends Tuple2<K4, R>> yieldingFunction) {
//        return null;
//    }
//
//    @Override
//    public <K1, K2, K3, R1, R2, R> ImmutableMap<K3, R> forEach3(Function<? super Tuple2<K, V>, ? extends Iterable<Tuple2<K1, R1>>> iterable1,
//                                                                BiFunction<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? extends Iterable<Tuple2<K2, R2>>> iterable2,
//                                                                Function3<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? super Tuple2<K2, R2>, ? extends Tuple2<K3, R>> yieldingFunction) {
//        return null;
//    }
//
//    @Override
//    public <K1, K2, K3, R1, R2, R> ImmutableMap<K3, R> forEach3(Function<? super Tuple2<K, V>, ? extends Iterable<Tuple2<K1, R1>>> iterable1,
//                                                                BiFunction<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? extends Iterable<Tuple2<K2, R2>>> iterable2,
//                                                                Function3<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? super Tuple2<K2, R2>, Boolean> filterFunction,
//                                                                Function3<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? super Tuple2<K2, R2>, ? extends Tuple2<K3, R>> yieldingFunction) {
//        return null;
//    }
//
//    @Override
//    public <K1, K2, R1, R> ImmutableMap<K2, R> forEach2(Function<? super Tuple2<K, V>, ? extends Iterable<Tuple2<K1, R1>>> iterable1,
//                                                        BiFunction<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? extends Tuple2<K2, R>> yieldingFunction) {
//        return null;
//    }
//
//    @Override
//    public <K1, K2, R1, R> ImmutableMap<K2, R> forEach2(Function<? super Tuple2<K, V>, ? extends Iterable<Tuple2<K1, R1>>> iterable1,
//                                                        BiFunction<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, Boolean> filterFunction,
//                                                        BiFunction<? super Tuple2<K, V>, ? super Tuple2<K1, R1>, ? extends Tuple2<K2, R>> yieldingFunction) {
//        return null;
//    }
//
//    @Override
//    public Map<K, V> toMap() {
//        return null;
//    }
//
//    @Override
//    public <K, V1> HashMap<K, V1> toHashMap(Function<? super Tuple2<K, V1>, ? extends K> keyMapper,
//                                            Function<? super Tuple2<K, V1>, ? extends V1> valueMapper) {
//        return null;
//    }
//
//    @Override
//    public <K, V1> Map<K, V1> toMap(Function<? super Tuple2<K, V1>, ? extends K> keyMapper,
//                                    Function<? super Tuple2<K, V1>, ? extends V1> valueMapper) {
//        return null;
//    }
//
//    @Override
//    public <K> Map<K, Tuple2<K, V>> toMap(Function<? super Tuple2<K, V>, ? extends K> keyMapper) {
//        return null;
//    }
//
//    @Override
//    public <T> Seq<T> toSeq(Function<? super Tuple2<? super K, ? super V>, ? extends T> fn) {
//        return null;
//    }
//
//    @Override
//    public <T> LazySeq<T> toLazySeq(Function<? super Tuple2<? super K, ? super V>, ? extends T> fn) {
//        return null;
//    }
//
//    @Override
//    public <U> Filters<U> ofType(Class<? extends U> type) {
//        return null;
//    }
//
//    @Override
//    public <R> R[] toArray(IntFunction<R[]> generator) {
//        return null;
//    }
//
//    @Override
//    public Object[] toArray() {
//        return new Object[0];
//    }
//
//    @Override
//    public <R> R to(Function<? super Iterable<? super Tuple2<K, V>>, ? extends R> fn) {
//        return null;
//    }
//
//    @Override
//    public BankersQueue<Tuple2<K, V>> bankersQueue() {
//        return null;
//    }
//
//    @Override
//    public TreeSet<Tuple2<K, V>> treeSet(Comparator<? super Tuple2<K, V>> comp) {
//        return null;
//    }
//
//    @Override
//    public HashSet<Tuple2<K, V>> hashSet() {
//        return null;
//    }
//
//    @Override
//    public Vector<Tuple2<K, V>> vector() {
//        return null;
//    }
//
//    @Override
//    public LazySeq<Tuple2<K, V>> lazySeq() {
//        return null;
//    }
//
//    @Override
//    public Seq<Tuple2<K, V>> seq() {
//        return null;
//    }
//
//    @Override
//    public NonEmptyList<Tuple2<K, V>> nonEmptyList(Supplier<Tuple2<K, V>> s) {
//        return null;
//    }
//
//
//
//    @Override
//    public <K> HashMap<K, Tuple2<K, V>> toHashMap(Function<? super Tuple2<K, V>, ? extends K> keyMapper) {
//        return null;
//    }
//
//    @Override
//    public <T extends Collection<Tuple2<K, V>>> T toCollection(Supplier<T> collectionFactory) {
//        return null;
//    }
//
//    @Override
//    public List<Tuple2<K, V>> toList() {
//        return null;
//    }
//
//    @Override
//    public Set<Tuple2<K, V>> toSet() {
//        return null;
//    }
//
//    @Override
//    public long countDistinct() {
//        return 0;
//    }
//
//    @Override
//    public <U> Option<Tuple2<K, V>> maxBy(Function<? super Tuple2<K, V>, ? extends U> function,
//                                           Comparator<? super U> comparator) {
//        return null;
//    }
//
//    @Override
//    public <U extends Comparable<? super U>> Option<Tuple2<K, V>> maxBy(Function<? super Tuple2<K, V>, ? extends U> function) {
//        return null;
//    }
//
//    @Override
//    public <U extends Comparable<? super U>> Option<Tuple2<K, V>> minBy(Function<? super Tuple2<K, V>, ? extends U> function) {
//        return null;
//    }
//
//    @Override
//    public <U extends Comparable<? super U>> Option<Tuple2<K, V>> minBy(Function<? super Tuple2<K, V>, ? extends U> function,
//                                                                         Comparator<? super U> comparator) {
//        return null;
//    }
//
//    @Override
//    public Option<Tuple2<K, V>> mode() {
//        return null;
//    }
//
//    @Override
//    public ReactiveSeq<Tuple2<Tuple2<K, V>, Integer>> occurrences() {
//        return null;
//    }
//
//    @Override
//    public double mean(ToDoubleFunction<Tuple2<K, V>> fn) {
//        return 0;
//    }
//
//    @Override
//    public Option<Tuple2<K, V>> median() {
//        return null;
//    }
//
//    @Override
//    public Seq<Tuple2<Tuple2<K, V>, BigDecimal>> withPercentiles() {
//        return null;
//    }
//
//    @Override
//    public Option<Tuple2<K, V>> atPercentile(double percentile) {
//        return null;
//    }
//
//    @Override
//    public double variance(ToDoubleFunction<Tuple2<K, V>> fn) {
//        return 0;
//    }
//
//    @Override
//    public double populationVariance(ToDoubleFunction<Tuple2<K, V>> fn) {
//        return 0;
//    }
//
//    @Override
//    public double stdDeviation(ToDoubleFunction<Tuple2<K, V>> fn) {
//        return 0;
//    }
//
//    @Override
//    public Option<Tuple2<K, V>> maximum(Comparator<? super Tuple2<K, V>> comparator) {
//        return null;
//    }
//
//    @Override
//    public Option<Tuple2<K, V>> minimum(Comparator<? super Tuple2<K, V>> comparator) {
//        return null;
//    }
//
//    @Override
//    public int sumInt(ToIntFunction<Tuple2<K, V>> fn) {
//        return 0;
//    }
//
//    @Override
//    public double sumDouble(ToDoubleFunction<Tuple2<K, V>> fn) {
//        return 0;
//    }
//
//    @Override
//    public long sumLong(ToLongFunction<Tuple2<K, V>> fn) {
//        return 0;
//    }
//
//    @Override
//    public <R, A> R collect(Collector<? super Tuple2<K, V>, A, R> collector) {
//        return null;
//    }
//
//    @Override
//    public long count() {
//        return 0;
//    }
//
//    @Override
//    public <K> HashMap<K, Vector<Tuple2<K, V>>> groupBy(Function<? super Tuple2<K, V>, ? extends K> classifier) {
//        return null;
//    }
//
//    @Override
//    public Option<Tuple2<K, V>> headOption() {
//        return Option.none();
//    }
//
//    @Override
//    public boolean startsWith(Iterable<Tuple2<K, V>> iterable) {
//        return false;
//    }
//
//    @Override
//    public boolean endsWith(Iterable<Tuple2<K, V>> iterable) {
//        return false;
//    }
//
//    @Override
//    public Tuple2<K, V> firstValue(Tuple2<K, V> alt) {
//        return alt;
//    }
//
//    @Override
//    public Tuple2<K, V> singleOrElse(Tuple2<K, V> alt) {
//        return alt;
//    }
//
//    @Override
//    public Maybe<Tuple2<K, V>> single(Predicate<? super Tuple2<K, V>> predicate) {
//        return Maybe.EMPTY;
//    }
//
//    @Override
//    public Maybe<Tuple2<K, V>> single() {
//        return Maybe.EMPTY;
//    }
//
//    @Override
//    public Maybe<Tuple2<K, V>> takeOne() {
//        return Maybe.EMPTY;
//    }
//
//    @Override
//    public Maybe<Tuple2<K, V>> elementAt(long index) {
//        return Maybe.EMPTY;
//    }
//
//    @Override
//    public Maybe<Long> indexOf(Predicate<? super Tuple2<K, V>> pred) {
//        return Maybe.EMPTY;
//    }
//
//    @Override
//    public Maybe<Long> lastIndexOf(Predicate<? super Tuple2<K, V>> pred) {
//        return Maybe.EMPTY;
//    }
//
//    @Override
//    public Maybe<Long> indexOfSlice(Iterable<? extends Tuple2<K, V>> slice) {
//        return Maybe.EMPTY;
//    }
//
//    @Override
//    public Maybe<Long> lastIndexOfSlice(Iterable<? extends Tuple2<K, V>> slice) {
//        return Maybe.EMPTY;
//    }
//
//    @Override
//    public boolean atLeast(int num,
//                           Predicate<? super Tuple2<K, V>> c) {
//        return false;
//    }
//
//    @Override
//    public boolean atMost(int num,
//                          Predicate<? super Tuple2<K, V>> c) {
//        return false;
//    }
//
//    @Override
//    public <R> Option<R> matchGet(K id,
//                                  Pattern<V, R> pattern) {
//        return Option.none();
//    }
//
//    @Override
//    public <R> Ent<K, R> matchMap(Pattern<V, R> patternFunc) {
//        return EmptyEnt.emptyEnt();
//    }

    @Override
    public Iterator<Tuple2<K, V>> iterator() {
        return Collections.emptyIterator();
    }

    private static enum EmptyEntSupplier implements Supplier<Ent<?, ?>> {
        INSTANCE;

        private final EmptyEnt<?, ?> emptyEnt = new EmptyEnt<>();

        @Override
        public Ent<?, ?> get() {
            return (Ent<?, ?>) emptyEnt;
        }
    }


}
