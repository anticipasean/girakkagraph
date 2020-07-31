package io.github.anticipasean.ent.func;

public interface Matcher<V> extends Clause<V> {

    static <S> Matcher<S> of(S subject) {
        return new Matcher<S>() {
            @Override
            public S get() {
                return subject;
            }
        };
    }

    static <S> MatchClause1<S> caseWhen(S subject) {
        return MatchClause1.of(() -> subject);
    }

    default MatchClause1<V> caseWhenValue() {
        return MatchClause1.of(this::subject);
    }

}
