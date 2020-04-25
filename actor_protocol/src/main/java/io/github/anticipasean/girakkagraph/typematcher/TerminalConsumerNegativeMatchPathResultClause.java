package io.github.anticipasean.girakkagraph.typematcher;

import java.util.function.Consumer;

/**
 * @author <a href="https://github.com/nurkiewicz/">Tomasz Nurkiewicz</a> of the original <a
 *     href="https://github.com/nurkiewicz/typeof">typeof lib</a>
 * @author Sean McCarron {@literal spmccarron@gmail.com} - renamed components for clarity, added
 *     predicate conditions, and orElseThrow and other exception handling logic
 */
class TerminalConsumerNegativeMatchPathResultClause<O, T> extends ConsumerPathResultClause<O, T> {

  TerminalConsumerNegativeMatchPathResultClause(
      ConsumerNegativeMatchPathResultClause<O> parent, O object, Class<T> expectedType) {
    super(parent, object, expectedType);
  }

  @Override
  public ConsumerNegativeMatchPathResultClause<O> then(Consumer<T> thenBlock) {
    return parent;
  }
}
