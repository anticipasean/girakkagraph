package io.github.anticipasean.girakkagraph.typematcher;

/**
 * @author <a href="https://github.com/nurkiewicz/">Tomasz Nurkiewicz</a> of the original <a
 *     href="https://github.com/nurkiewicz/typeof">typeof lib</a>
 * @author Sean McCarron {@literal spmccarron@gmail.com} - renamed components for clarity, added
 *     predicate conditions, and orElseThrow and other exception handling logic
 */
public class TypeMatcherException extends RuntimeException {

  public TypeMatcherException() {
  }

  public TypeMatcherException(String message) {
    super(message);
  }

  public TypeMatcherException(String message, Throwable cause) {
    super(message, cause);
  }

  public TypeMatcherException(Throwable cause) {
    super(cause);
  }

  public TypeMatcherException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
