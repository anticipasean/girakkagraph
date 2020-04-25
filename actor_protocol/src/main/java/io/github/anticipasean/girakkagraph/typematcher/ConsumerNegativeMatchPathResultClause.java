package io.github.anticipasean.girakkagraph.typematcher;

import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author <a href="https://github.com/nurkiewicz/">Tomasz Nurkiewicz</a> of the original <a
 *     href="https://github.com/nurkiewicz/typeof">typeof lib</a>
 * @author Sean McCarron {@literal spmccarron@gmail.com} - renamed components for clarity, added
 *     predicate conditions, and orElseThrow and other exception handling logic
 */
public class ConsumerNegativeMatchPathResultClause<O> {

  private final O object;

  ConsumerNegativeMatchPathResultClause(O object) {
    this.object = object;
  }

  public <T> ConsumerPathResultClause<O, T> is(Class<T> type) {
    return new ConsumerPathResultClause<>(this, object, type);
  }

  public <T> ConsumerPathResultClause<O, T> is(Class<T> type, Predicate<T> andMeetsCondition) {
    return new ConsumerPathResultClause<>(this, object, type, andMeetsCondition);
  }
  /**
   * Unhappy consumer path: If this is called, no match was made but this final consumer should
   * handle the object supplied
   *
   * @param orElseBlock - consumer of the object when no match has been made to any of the supplied
   *     types
   */
  public void orElse(Consumer<O> orElseBlock) {
    orElseBlock.accept(object);
  }

  /**
   * Unhappy consumer path: If this is called, no match was made
   *
   * @throws NoSuchElementException - when no match has been made
   */
  public void orElseThrow() {
    orElseThrow(() -> new NoSuchElementException(TypeMatcher.noMatchFoundMessageForObject(object)));
  }

  /**
   * Unhappy consumer path: If this is called, no match was made and the exception supplied is
   * thrown
   *
   * @param exceptionSupplier
   * @param <X> - type of exception thrown
   * @throws X - exception of type X if supplier given, else {@link NullPointerException} for
   *     supplying a null value for an exception supplier
   */
  public <X extends Throwable> void orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
    if (exceptionSupplier != null) {
      throw exceptionSupplier.get();
    }
    throw new NullPointerException(TypeMatcher.noExceptionSupplierMessageForObject(object));
  }
}
