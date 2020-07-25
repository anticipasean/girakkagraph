package io.github.anticipasean.ent;

import cyclops.data.tuple.Tuple2;
import io.github.anticipasean.ent.pattern.PatternMatching;
import java.math.BigDecimal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatternMatchingTest {

    private Logger logger = LoggerFactory.getLogger(PatternMatchingTest.class);

    @Test
    public void ifFitsConditionTest() {
        Assertions.assertEquals("eq 5",
                                PatternMatching.forObject(5)
                                               .ifFits(integer -> integer > 6)
                                               .then(integer -> "gt 6")
                                               .ifFits(integer -> integer < 5)
                                               .then(integer -> "lt 5")
                                               .ifFits(integer -> integer == 5)
                                               .then(integer -> "eq 5")
                                               .orElse("no Match"));
    }

    @Test
    public void ifKeyFitsAndValueOfTypeConditionTest() {
        Number numberFive = Integer.valueOf(5);
        String patternMatchingResult = PatternMatching.forKeyValuePair("five",
                                                                       numberFive)
                                                      .ifKeyFitsAndValueOfType(s -> s.equalsIgnoreCase("one-half"),
                                                                               Double.class)
                                                      .andValueFits(aDouble -> aDouble.compareTo(0.5D) == 0)
                                                      .then((s, aDouble) -> "0.5")
                                                      .ifKeyFitsAndValueOfType(s -> "five".equalsIgnoreCase(s),
                                                                               BigDecimal.class)
                                                      .and(bigDecForm -> BigDecimal.valueOf(5.0000D)
                                                                                   .equals(bigDecForm))
                                                      .then((s, bigDecimalForm) -> "5.0000")
                                                      .ifKeyFitsAndValueOfType(s -> "five".equalsIgnoreCase(s),
                                                                               Integer.class)
                                                      .and(integer -> 5 == integer)
                                                      .then((s, integerForm) -> "5")
                                                      .orElse("No Match");
        logger.info("pattern_matching_result: [ expected: {} as {}, actual: {} as {} ]",
                    "5",
                    String.class,
                    patternMatchingResult,
                    patternMatchingResult.getClass());
        Assertions.assertEquals("5",
                                patternMatchingResult);


    }

    @Test
    public void ifKeyValuePairFitsConditionTest() {
        Number numberFive = Integer.valueOf(5);
        Tuple2<String, String> patternMatchingResult = PatternMatching.forKeyValuePair("five",
                                                                                       numberFive)
                                                                      .ifKeyValueFits((s, number) -> "one-half".equals(s)
                                                                          && 0.5D == number.doubleValue())
                                                                      .then((s, aDouble) -> Tuple2.of(s,
                                                                                                      "0.5"))
                                                                      .ifKeyValueFits((s, number) -> "precise 5".equals(s)
                                                                          && BigDecimal.valueOf(5.000D)
                                                                                       .equals(number))
                                                                      .then((s, number) -> Tuple2.of(s,
                                                                                                     BigDecimal.valueOf(5.0000D)
                                                                                                               .toEngineeringString()))
                                                                      .ifKeyValueFits((s, number) -> "five".equals(s)
                                                                          && 5 == number.intValue())
                                                                      .then((s, number) -> Tuple2.of(s,
                                                                                                     Integer.valueOf(5)
                                                                                                            .toString()))
                                                                      .orElse(Tuple2.of("noMatch",
                                                                                        "noMatch"));
        logger.info("pattern_matching_result: [ expected: {} as {}, actual: {} as {} ]",
                    Tuple2.of("five",
                              "5"),
                    Tuple2.class,
                    patternMatchingResult,
                    patternMatchingResult.getClass());
        Assertions.assertEquals(Tuple2.of("five",
                                          "5"),
                                patternMatchingResult);


    }
}