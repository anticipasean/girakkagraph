package io.github.anticipasean.girakkagraph.typematcher;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author <a href="https://github.com/nurkiewicz/">Tomasz Nurkiewicz</a> of the original <a
 *     href="https://github.com/nurkiewicz/typeof">typeof lib</a>
 * @author Sean McCarron {@literal spmccarron@gmail.com} - renamed components for clarity, added
 *     predicate conditions, and orElseThrow and other exception handling logic
 */
class TerminalFunctionalPositiveMatchPathThen<O, R> extends FunctionalNegativeMatchPathThen<O, R> {

  private final R result;

  TerminalFunctionalPositiveMatchPathThen(O object, R result) {
    super(object);
    this.result = result;
  }

  @Override
  public <T> FunctionalPathResultClause<O, T, R> is(Class<T> expectedType) {
    return new TerminalFunctionalPathResultClause<>(object, this.result);
  }

  @Override
  public <T> FunctionalPathResultClause<O, T, R> is(Class<T> expectedType, Predicate<T> condition) {
    return new TerminalFunctionalPathResultClause<>(object, this.result);
  }

  @Override
  public R get() {
    return this.result;
  }

  @Override
  public R orElse(R result) {
    return this.result;
  }

  @Override
  public R orElse(Function<O, R> resultFun) {
    return this.result;
  }

  /**
   * Happy function path: the parent method is overridden and this method merely returns the matched
   * result
   *
   * @return R - result since Terminal(Then|Return) classes represent happy path
   */
  @Override
  public R orElseThrow() {
    return this.result;
  }

  /**
   * Happy function path: the parent method is overridden and this method merely returns the matched
   * result
   *
   * @param exceptionSupplier
   * @param <X> - exception of some type
   * @return R - result since Terminal(Then|Return) classes represent happy path
   * @throws X - won't actually throw the supplied exception if on this path
   */
  @Override
  public <X extends Throwable> R orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
    return this.result;
  }
}
