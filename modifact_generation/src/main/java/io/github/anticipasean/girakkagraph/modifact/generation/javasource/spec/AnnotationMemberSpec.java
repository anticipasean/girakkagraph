package io.github.anticipasean.girakkagraph.modifact.generation.javasource.spec;

import static cyclops.matching.Api.Any;
import static cyclops.matching.Api.Case;
import static cyclops.matching.Api.Match;
import static java.lang.Character.isISOControl;

import com.squareup.javapoet.AnnotationSpec;
import cyclops.data.tuple.Tuple2;
import java.util.function.UnaryOperator;
import org.immutables.value.Value;

@Value.Immutable
public interface AnnotationMemberSpec {

  /**
   * From com.squareup.javapoet.Util#characterLiteralWithoutSingleQuotes(char)
   *
   * @param c
   * @return
   */
  static String characterLiteralWithoutSingleQuotes(char c) {
    // see https://docs.oracle.com/javase/specs/jls/se7/html/jls-3.html#jls-3.10.6
    switch (c) {
      case '\b':
        return "\\b"; /* \u0008: backspace (BS) */
      case '\t':
        return "\\t"; /* \u0009: horizontal tab (HT) */
      case '\n':
        return "\\n"; /* \u000a: linefeed (LF) */
      case '\f':
        return "\\f"; /* \u000c: form feed (FF) */
      case '\r':
        return "\\r"; /* \u000d: carriage return (CR) */
      case '\"':
        return "\""; /* \u0022: double quote (") */
      case '\'':
        return "\\'"; /* \u0027: single quote (') */
      case '\\':
        return "\\\\"; /* \u005c: backslash (\) */
      default:
        return isISOControl(c) ? String.format("\\u%04x", (int) c) : Character.toString(c);
    }
  }

  @Value.Parameter
  String name();

  @Value.Derived
  default Tuple2<String, Object> javaPoetFormatValueTuple() {
    return Match(value())
        .with(
            Case(o -> o instanceof Class, o -> Tuple2.of("$T.class", value())),
            Case(
                o -> o instanceof Enum,
                o ->
                    Tuple2.of(
                        "$T.$L", new Object[] {value().getClass(), ((Enum<?>) value()).name()})),
            Case(o -> o instanceof String, o -> Tuple2.of("$S", value())),
            Case(o -> o instanceof Float, o -> Tuple2.of("$Lf", value())),
            Case(
                o -> o instanceof Character,
                o -> Tuple2.of("'$L'", characterLiteralWithoutSingleQuotes((char) value()))),
            Any(() -> Tuple2.of("$L", value())));
  }

  @Value.Parameter
  Object value();

  @Value.Derived
  default UnaryOperator<AnnotationSpec.Builder> annotationMemberAnnotationSpecBuilderUpdater() {
    return builder -> {
      if (value() instanceof Enum && javaPoetFormatValueTuple()._2() instanceof Object[]) {
        Object[] classAndEnumObjectArr = (Object[]) javaPoetFormatValueTuple()._2();
        return builder.addMember(
            name(),
            javaPoetFormatValueTuple()._1(),
            classAndEnumObjectArr[0],
            classAndEnumObjectArr[1]);
      }
      return builder.addMember(
          name(), javaPoetFormatValueTuple()._1(), javaPoetFormatValueTuple()._2());
    };
  }
}
