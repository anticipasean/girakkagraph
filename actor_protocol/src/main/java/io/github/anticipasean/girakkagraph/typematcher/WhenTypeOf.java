package io.github.anticipasean.girakkagraph.typematcher;

import java.util.function.Predicate;

/**
 * @author <a href="https://github.com/nurkiewicz/">Tomasz Nurkiewicz</a> of the original <a
 *     href="https://github.com/nurkiewicz/typeof">typeof lib</a>
 * @author Sean McCarron {@literal spmccarron@gmail.com} - renamed components for clarity, added
 *     predicate conditions, and orElseThrow and other exception handling logic
 */
public class WhenTypeOf<O> {

  private final O object;

  WhenTypeOf(O object) {
    this.object = object;
  }

  public <T> FirstResultClause<O, T> is(Class<T> type) {
    return new FirstResultClause<>(new ConsumerNegativeMatchPathResultClause<>(object), object, type);
  }

  public <T> FirstResultClause<O, T> is(Class<T> type, Predicate<T> andMeetsCondition) {
    return new FirstResultClause<>(
        new ConsumerNegativeMatchPathResultClause<>(object), object, type, andMeetsCondition);
  }
}
