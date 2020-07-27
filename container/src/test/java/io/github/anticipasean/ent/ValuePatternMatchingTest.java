package io.github.anticipasean.ent;

import cyclops.data.tuple.Tuple2;
import io.github.anticipasean.ent.pattern.PatternMatching;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ValuePatternMatchingTest {

    private Logger logger = LoggerFactory.getLogger(ValuePatternMatchingTest.class);

    @Test
    public void ifFitsConditionTest() {
        Assert.assertEquals("eq 5",
                            PatternMatching.forValue(5)
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
        Tuple2<String, String> patternMatchingResult = PatternMatching.forKeyValuePair("five",
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
        logger.info("pattern_matching_result: [ value expected: {} as {}, actual: {} as {} ]",
                    "5",
                    String.class,
                    patternMatchingResult._2(),
                    patternMatchingResult._2()
                                         .getClass());
        Assert.assertEquals("5",
                            patternMatchingResult._2());


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
                                                                                        "noMatch"))
                                                                      .fold((s, stringStringTuple2) -> stringStringTuple2);
        logger.info("pattern_matching_result: [ expected: {} as {}, actual: {} as {} ]",
                    Tuple2.of("five",
                              "5"),
                    Tuple2.class,
                    patternMatchingResult,
                    patternMatchingResult.getClass());
        Assert.assertEquals(Tuple2.of("five",
                                      "5"),
                            patternMatchingResult);


    }

    @Test
    public void iterableMatchingTest() {
        Set<Integer> set = new HashSet<Integer>();
        set.add(1);
        Object setObject = set;
        Supplier<Number> numberSupplierResult = PatternMatching.forValue(setObject)
                                                               .ifIterableOver(Float.class)
                                                               .and(floats -> floats.allMatch(aFloat -> aFloat > 1.0f))
                                                               .then(floats -> (Supplier<Number>) () -> floats.findFirst()
                                                                                                              .orElse(2.0F))
                                                               .ifIterableOver(BigDecimal.class)
                                                               .then(bigDecimals -> () -> bigDecimals.max(BigDecimal::compareTo)
                                                                                                     .orElse(BigDecimal.TEN))
                                                               .ifIterableOver(Integer.class)
                                                               .then(integers -> () -> integers.findFirst()
                                                                                               .orElse(7))
                                                               .orElse(() -> 8);
        Assert.assertEquals(numberSupplierResult.get(),
                            (Integer) 1);
    }

    @Test
    public void iterableMatchingPlusPredicateClauseTest() {
        Set<Integer> set = new HashSet<Integer>();
        set.add(1);
        Object setObject = set;
        Supplier<Number> numberSupplierResult = PatternMatching.forValue(setObject)
                                                               .ifIterableOver(Float.class)
                                                               .and(floats -> floats.allMatch(aFloat -> aFloat > 1.0f))
                                                               .then(floats -> (Supplier<Number>) () -> floats.findFirst()
                                                                                                              .orElse(2.0F))
                                                               .ifIterableOver(BigDecimal.class)
                                                               .then(bigDecimals -> () -> bigDecimals.max(BigDecimal::compareTo)
                                                                                                     .orElse(BigDecimal.TEN))
                                                               .ifIterableOver(Integer.class)
                                                               .and(integers -> integers.findFirst()
                                                                                        .orElse(-1) == 40)
                                                               .then(integers -> () -> integers.findFirst()
                                                                                               .orElse(7))
                                                               .ifIterableOver(Integer.class)
                                                               .and(integers -> integers.findFirst()
                                                                                        .orElse(-1) == 1)
                                                               .then(integers -> () -> integers.findFirst()
                                                                                               .orElse(-1))
                                                               .orElse(() -> 8);
        Assert.assertEquals(numberSupplierResult.get(),
                            (Integer) 1);
    }

}