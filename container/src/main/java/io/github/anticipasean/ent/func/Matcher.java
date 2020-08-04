package io.github.anticipasean.ent.func;

import cyclops.data.tuple.Tuple2;
import io.github.anticipasean.ent.func.pair.MatchClause2;
import io.github.anticipasean.ent.func.single.MatchClause1;

public interface Matcher<V> extends Clause<V> {

    static <S> Matcher1<S> of(S subject) {
        return new Matcher1<S>() {
            @Override
            public S get() {
                return subject;
            }
        };
    }

    static <K, V> Matcher2<K, V> of(K key,
                                    V value) {
        return new Matcher2<K, V>() {
            @Override
            public Tuple2<K, V> get() {
                return Tuple2.of(key,
                                 value);
            }
        };
    }

    static <K, V> Matcher2<K, V> of(Tuple2<K, V> value) {
        return new Matcher2<K, V>() {
            @Override
            public Tuple2<K, V> get() {
                return value;
            }
        };
    }

    static <S> MatchClause1<S> caseWhen(S subject) {
        return MatchClause1.of(() -> subject);
    }

    static <K, V> MatchClause2<K, V> caseWhen(K key,
                                              V value) {
        return MatchClause2.of(() -> Tuple2.of(key,
                                               value));
    }

    static <K, V> MatchClause2<K, V> caseWhen(Tuple2<K, V> keyValueTuple) {
        return MatchClause2.of(() -> keyValueTuple);
    }

    static interface Matcher1<V> extends Matcher<V> {

        default MatchClause1<V> caseWhenValue() {
            return MatchClause1.of(this::subject);
        }
    }

    static interface Matcher2<K, V> extends Matcher<Tuple2<K, V>> {

        default MatchClause2<K, V> caseWhenKeyValue() {
            return MatchClause2.of(this::subject);
        }
    }
}
