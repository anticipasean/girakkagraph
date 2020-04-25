package io.github.anticipasean.girakkagraph.typematcher;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author <a href="https://github.com/nurkiewicz/">Tomasz Nurkiewicz</a> of the original <a
 *     href="https://github.com/nurkiewicz/typeof">typeof lib</a>
 * @author Sean McCarron {@literal spmccarron@gmail.com} - renamed components for clarity, added
 *     predicate conditions, and orElseThrow and other exception handling logic
 */
public class TerminalConsumerPositiveMatchPathResultClause<O> extends
    ConsumerNegativeMatchPathResultClause<O> {

  TerminalConsumerPositiveMatchPathResultClause() {
    super(null);
  }

  @Override
  public <T> ConsumerPathResultClause<O, T> is(Class<T> type) {
    return new TerminalConsumerNegativeMatchPathResultClause<>(this, null, null);
  }

  /** Happy path: Override parent and do nothing */
  @Override
  public void orElse(Consumer<O> orElseBlock) {
    // no-op
  }

  /** Happy path: Override parent and do nothing */
  @Override
  public void orElseThrow() {
    // no-op
  }

  /**
   * Happy path: Override parent and do nothing
   *
   * @param exceptionSupplier
   * @param <X>
   * @throws X
   */
  @Override
  public <X extends Throwable> void orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
    // no-op
  }
}
