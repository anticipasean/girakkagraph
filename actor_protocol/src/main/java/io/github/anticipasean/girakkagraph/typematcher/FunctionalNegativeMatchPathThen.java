package io.github.anticipasean.girakkagraph.typematcher;

import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author <a href="https://github.com/nurkiewicz/">Tomasz Nurkiewicz</a> of the original <a
 *     href="https://github.com/nurkiewicz/typeof">typeof lib</a>
 * @author Sean McCarron {@literal spmccarron@gmail.com} - renamed components for clarity, added
 *     predicate conditions, and orElseThrow and other exception handling logic
 */
public class FunctionalNegativeMatchPathThen<O, R> {

  final O object;

  FunctionalNegativeMatchPathThen(O object) {
    this.object = object;
  }

  public <T> FunctionalPathResultClause<O, T, R> is(Class<T> expectedType) {
    return new FunctionalPathResultClause<>(object, expectedType);
  }

  public <T> FunctionalPathResultClause<O, T, R> is(Class<T> expectedType, Predicate<T> condition) {
    return new FunctionalPathResultClause<>(object, expectedType, condition);
  }

  /**
   * Unhappy function path: If this is called, no match was made, so there isn't an object of the
   * expected result type to return
   *
   * @return no expected result of type R but will instead throw a {@link NoSuchElementException}
   */
  public R get() {
    return orElseThrow();
  }

  /**
   * Unhappy function path: If this is called, no match was made, but the object will be mapped
   * using the supplied function to a result of the expected type
   *
   * @param resultFun - mapper function yielding a result of the expected type R
   * @return result of the expected type R, the result of applying the mapper function
   */
  public R orElse(Function<O, R> resultFun) {
    return resultFun.apply(object);
  }

  /**
   * Unhappy function path: If this is called, no match was made, but the object of the expected
   * result type R supplied will be returned instead
   *
   * @param result - object of the expected result type R
   * @return object supplied as a parameter to this method of expected result type R
   */
  public R orElse(R result) {
    return result;
  }

  /**
   * Unhappy function path: If this is called, no match was made
   *
   * @throws NoSuchElementException - when no match has been made
   */
  public R orElseThrow() {
    return orElseThrow(
        () -> new NoSuchElementException(TypeMatcher.noMatchFoundMessageForObject(object)));
  }

  /**
   * Unhappy function path: If this is called, no match was made and the exception supplied is
   * thrown
   *
   * @param exceptionSupplier
   * @param <X> - type of exception thrown
   * @throws X - exception of type X if supplier given, else {@link NullPointerException} for not
   *     supplying an exception supplier
   */
  public <X extends Throwable> R orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
    if (exceptionSupplier != null) {
      throw exceptionSupplier.get();
    }
    throw new NullPointerException(TypeMatcher.noExceptionSupplierMessageForObject(object));
  }
}
