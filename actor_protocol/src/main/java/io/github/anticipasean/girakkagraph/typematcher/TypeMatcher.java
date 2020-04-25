package io.github.anticipasean.girakkagraph.typematcher;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author <a href="https://github.com/nurkiewicz/">Tomasz Nurkiewicz</a> of the original <a
 *     href="https://github.com/nurkiewicz/typeof">typeof lib</a>
 * @author Sean McCarron {@literal spmccarron@gmail.com} - renamed components for clarity, added
 *     predicate conditions, and orElseThrow and other exception handling logic
 */
public class TypeMatcher {

  public static <O> WhenTypeOf<O> whenTypeOf(O object) {
    return new WhenTypeOf<>(object);
  }

  static <O> String noMatchFoundMessageForObject(O object) {
    return String.format(
        "None of the types indicated for type matching matched this object: [ %s ]\n"
            + "Object appears to be of type: [ class.name: %s, class.interfaces[:].name: [ %s ] ]",
        object,
        buildSizeLimitedObjectToString(object),
        buildObjectAppearsToBeOfTypeMessageComponent(object));
  }

  private static <O> String buildSizeLimitedObjectToString(O object) {
    int arbitraryObjectToStringLengthCutOffPoint = 256;
    StringBuilder sb = new StringBuilder(Objects.toString(object, "null"));
    if (sb.length() > arbitraryObjectToStringLengthCutOffPoint) {
      String ellipsis = "...";
      sb.setLength(arbitraryObjectToStringLengthCutOffPoint - ellipsis.length());
      sb.append(ellipsis);
    }
    return sb.toString();
  }

  private static <O> String buildObjectAppearsToBeOfTypeMessageComponent(O object) {
    return String.format(
        "Object appears to be of type: [ class.name: %s, class.interfaces[:].name: [ %s ] ]",
        object == null ? "null" : object.getClass().getName(),
        object == null
            ? "null"
            : Arrays.stream(object.getClass().getInterfaces())
                .map(Class::getName)
                .collect(Collectors.joining(", ")));
  }

  static <O> String noExceptionSupplierMessageForObject(O object) {
    return String.format(
        "no exception supplier was given for the case in which object [ %s ] does "
            + "not match any of the types specified\n%s",
        buildSizeLimitedObjectToString(object),
        buildObjectAppearsToBeOfTypeMessageComponent(object));
  }

  static <S, T> boolean objectMatchesAnExpectedTypeAndMeetsItsConditionIfPresent(
      S object, Class<T> expectedType, Predicate<T> condition) {
    return isObjectAssignableToExpectedType(object, expectedType)
        && Optional.ofNullable(condition)
            .map(predicate -> predicate.test(castObjectUsingMatchedType(object, expectedType)))
            .orElse(Boolean.TRUE);
  }

  static <O, T> boolean isObjectAssignableToExpectedType(O object, Class<T> expectedType) {
    try {
      Objects.requireNonNull(expectedType, "expectedType");
      return object != null && expectedType.isAssignableFrom(object.getClass());
    } catch (Exception e) {
      Supplier<String> messageSupplier =
          () ->
              String.format(
                  "an error occurred when attempting to assess whether object [ %s ] is "
                      + "assignable to type [ %s ]",
                  buildSizeLimitedObjectToString(object), expectedType);
      throw new TypeMatcherException(messageSupplier.get(), e);
    }
  }

  static <O, T> T castObjectUsingMatchedType(O object, Class<T> matchedType) {
    try {
      return matchedType.cast(object);
    } catch (Exception e) {
      Supplier<String> messageSupplier =
          () ->
              String.format(
                  "an error occurred when attempting to cast object [ %s ]",
                  buildSizeLimitedObjectToString(object));
      throw new TypeMatcherException(messageSupplier.get(), e);
    }
  }
}
