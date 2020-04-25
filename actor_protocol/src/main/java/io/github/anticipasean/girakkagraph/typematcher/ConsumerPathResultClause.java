package io.github.anticipasean.girakkagraph.typematcher;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author <a href="https://github.com/nurkiewicz/">Tomasz Nurkiewicz</a> of the original <a
 *     href="https://github.com/nurkiewicz/typeof">typeof lib</a>
 * @author Sean McCarron {@literal spmccarron@gmail.com} - renamed components for clarity, added
 *     predicate conditions, and orElseThrow and other exception handling logic
 */
public class ConsumerPathResultClause<O, T> {

  final ConsumerNegativeMatchPathResultClause<O> parent;
  private final O object;
  private final Class<T> expectedType;
  private final Predicate<T> condition;

  ConsumerPathResultClause(ConsumerNegativeMatchPathResultClause<O> parent, O object, Class<T> expectedType) {
    this.parent = parent;
    this.object = object;
    this.expectedType = expectedType;
    this.condition = null;
  }

  ConsumerPathResultClause(
      ConsumerNegativeMatchPathResultClause<O> parent,
      O object,
      Class<T> expectedType,
      Predicate<T> condition) {
    this.parent = parent;
    this.object = object;
    this.expectedType = expectedType;
    this.condition = condition;
  }

  public ConsumerNegativeMatchPathResultClause<O> then(Consumer<T> thenBlock) {
    if (TypeMatcher.objectMatchesAnExpectedTypeAndMeetsItsConditionIfPresent(
        object, expectedType, condition)) {
      thenBlock.accept(TypeMatcher.castObjectUsingMatchedType(object, expectedType));
      return new TerminalConsumerPositiveMatchPathResultClause<>();
    }
    return parent;
  }
}
