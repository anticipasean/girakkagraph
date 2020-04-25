package io.github.anticipasean.girakkagraph.typematcher;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author <a href="https://github.com/nurkiewicz/">Tomasz Nurkiewicz</a> of the original <a
 *     href="https://github.com/nurkiewicz/typeof">typeof lib</a>
 * @author Sean McCarron {@literal spmccarron@gmail.com} - renamed components for clarity, added
 *     predicate conditions, and orElseThrow and other exception handling logic
 */
public class FunctionalPathResultClause<S, T, R> {

  final S object;
  private final Class<T> expectedType;
  private final Predicate<T> condition;

  FunctionalPathResultClause(S object, Class<T> expectedType) {
    this.object = object;
    this.expectedType = expectedType;
    this.condition = null;
  }

  FunctionalPathResultClause(S object, Class<T> expectedType, Predicate<T> condition) {
    this.object = object;
    this.expectedType = expectedType;
    this.condition = condition;
  }

  public FunctionalNegativeMatchPathThen<S, R> thenApply(Function<T, R> resultFun) {
    if (TypeMatcher.objectMatchesAnExpectedTypeAndMeetsItsConditionIfPresent(
        object, expectedType, condition)) {
      final R result = resultFun.apply(TypeMatcher.castObjectUsingMatchedType(object, expectedType));
      return new TerminalFunctionalPositiveMatchPathThen<>(object, result);
    }
    return new FunctionalNegativeMatchPathThen<>(object);
  }

  public FunctionalNegativeMatchPathThen<S, R> thenReturn(R result) {
    if (TypeMatcher.objectMatchesAnExpectedTypeAndMeetsItsConditionIfPresent(
        object, expectedType, condition)) {
      return new TerminalFunctionalPositiveMatchPathThen<>(object, result);
    }
    return new FunctionalNegativeMatchPathThen<>(object);
  }
}
