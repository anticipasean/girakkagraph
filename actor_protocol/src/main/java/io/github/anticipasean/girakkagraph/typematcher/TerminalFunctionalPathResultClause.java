package io.github.anticipasean.girakkagraph.typematcher;

import java.util.function.Function;

/**
 * @author <a href="https://github.com/nurkiewicz/">Tomasz Nurkiewicz</a> of the original <a
 *     href="https://github.com/nurkiewicz/typeof">typeof lib</a>
 * @author Sean McCarron {@literal spmccarron@gmail.com} - renamed components for clarity, added
 *     predicate conditions, and orElseThrow and other exception handling logic
 */
class TerminalFunctionalPathResultClause<S, T, R> extends FunctionalPathResultClause<S, T, R> {

  private final R result;

  TerminalFunctionalPathResultClause(S object, R result) {
    super(object, null);
    this.result = result;
  }

  @Override
  public FunctionalNegativeMatchPathThen<S, R> thenApply(Function<T, R> resultFun) {
    return new TerminalFunctionalPositiveMatchPathThen<>(object, result);
  }

  @Override
  public FunctionalNegativeMatchPathThen<S, R> thenReturn(R result) {
    return new TerminalFunctionalPositiveMatchPathThen<>(object, this.result);
  }
}
