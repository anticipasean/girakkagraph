package io.github.anticipasean.ent;

import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import io.github.anticipasean.ent.func.Matcher;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
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
                            Matcher.caseWhen(5)
                                   .fits(integer -> integer > 6)
                                   .then(integer -> "gt 6")
                                   .fits(integer -> integer < 5)
                                   .then(integer -> "lt 5")
                                   .fits(integer -> integer == 5)
                                   .then(integer -> "eq 5")
                                   .elseDefault("no Match"));
    }

    @Test
    public void keyFitsConditionTest() {
        Number numberFive = Integer.valueOf(5);
        Tuple2<String, String> patternMatchingResult = Matcher.caseWhen("five",
                                                                        numberFive)
                                                              .valueOfTypeAndBothFit(Double.class,
                                                                                     (s, vDoubl) -> s.equalsIgnoreCase("one-half")
                                                                                         && vDoubl.compareTo(0.5D) == 0)
                                                              .then((s, aDouble) -> Tuple2.of(s,
                                                                                              "0.5"))
                                                              .valueOfTypeAndBothFit(BigDecimal.class,
                                                                                     (s, bigD) -> "five".equalsIgnoreCase(s)
                                                                                         && BigDecimal.valueOf(5.0000D)
                                                                                                      .equals(bigD))
                                                              .then(Function.identity(), bigDecimalForm -> "5.0000")
                                                              .valueOfTypeAndBothFit(Integer.class,
                                                                                     (s, i) -> "five".equalsIgnoreCase(s)
                                                                                         && 5 == i)
                                                              .then(Function.identity(), (integerForm) -> "5")
                                                              .elseDefault(Tuple2.of("No match",
                                                                                     "No match"));
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
        Tuple2<String, String> patternMatchingResult = Matcher.caseWhen("five",
                                                                        numberFive)
                                                              .bothFit((s, number) -> "one-half".equals(s)
                                                                  && 0.5D == number.doubleValue())
                                                              .then((s, aDouble) -> Tuple2.of(s,
                                                                                              "0.5"))
                                                              .bothFit((s, number) -> "precise 5".equals(s)
                                                                  && BigDecimal.valueOf(5.000D)
                                                                               .equals(number))
                                                              .then((s, number) -> Tuple2.of(s,
                                                                                             BigDecimal.valueOf(5.0000D)
                                                                                                       .toEngineeringString()))
                                                              .bothFit((s, number) -> "five".equals(s) && 5 == number.intValue())
                                                              .then((s, number) -> Tuple2.of(s,
                                                                                             Integer.valueOf(5)
                                                                                                    .toString()))
                                                              .elseDefault(Tuple2.of("noMatch",
                                                                                     "noMatch"));
        logger.info("pattern_matching_result: [ expected: {} as {}, actual: {} as {} ]",
                    Tuple2.of("five",
                              "5"),
                    Tuple2.class,
                    patternMatchingResult,
                    patternMatchingResult.getClass());
        Assert.assertEquals(patternMatchingResult,
                            Tuple2.of("five",
                                      "5"));


    }

    @Test
    public void iterableMatchingTest() {
        Set<Integer> set = new HashSet<Integer>();
        set.add(1);
        Object setObject = set;
        Supplier<Number> numberSupplierResult = Matcher.caseWhen(setObject)
                                                       .isIterableOverAnd(Float.class,
                                                                          floats -> floats.allMatch(aFloat -> aFloat > 1.0f))
                                                       .then(floats -> (Supplier<Number>) () -> floats.findFirst()
                                                                                                      .orElse(2.0F))
                                                       .isIterableOver(BigDecimal.class)
                                                       .then(bigDecimals -> () -> bigDecimals.max(BigDecimal::compareTo)
                                                                                             .orElse(BigDecimal.TEN))
                                                       .isIterableOver(Integer.class)
                                                       .then(integers -> () -> integers.findFirst()
                                                                                       .orElse(7))
                                                       .elseDefault(() -> 8);
        Assert.assertEquals(numberSupplierResult.get(),
                            (Integer) 1);
    }

    @Test
    public void iterableMatchingPlusPredicateClauseTest() {
        Set<Integer> set = new HashSet<Integer>();
        set.add(1);
        Object setObject = set;
        Supplier<Number> numberSupplierResult = Matcher.caseWhen(setObject)
                                                       .isIterableOverAnd(Float.class,
                                                                          floats -> floats.allMatch(aFloat -> aFloat > 1.0f))
                                                       .then(floats -> (Supplier<Number>) () -> floats.findFirst()
                                                                                                      .orElse(2.0F))
                                                       .isIterableOver(BigDecimal.class)
                                                       .then(bigDecimals -> () -> bigDecimals.max(BigDecimal::compareTo)
                                                                                             .orElse(BigDecimal.TEN))
                                                       .isIterableOverAnd(Integer.class,
                                                                          integers -> integers.findFirst()
                                                                                              .orElse(-1) == 40)
                                                       .then(integers -> () -> integers.findFirst()
                                                                                       .orElse(7))
                                                       .isIterableOverAnd(Integer.class,
                                                                          integers -> integers.findFirst()
                                                                                              .orElse(-1) == 1)
                                                       .then(integers -> () -> integers.findFirst()
                                                                                       .orElse(-1))
                                                       .elseDefault(() -> 8);
        Assert.assertEquals(numberSupplierResult.get(),
                            (Integer) 1);
    }

    @Test
    public void functionalMatcherPositiveTest() {

        BigDecimal bigDec = Matcher.caseWhen(40)
                                   .isOfType(BigDecimal.class)
                                   .then(bigDecimal -> BigDecimal.TEN)
                                   .isOfType(Integer.class)
                                   .then(integer -> BigDecimal.valueOf(integer))
                                   .isOfType(Float.class)
                                   .then(aFloat -> BigDecimal.valueOf(2.2))
                                   .elseDefault(BigDecimal.ONE);
        Assert.assertEquals(bigDec,
                            BigDecimal.valueOf(40));
    }

    @Test
    public void functionalMatcherNegativeTest() {

        BigDecimal bigDec = Matcher.caseWhen(BigInteger.valueOf(20))
                                   .isOfType(BigDecimal.class)
                                   .then(bigDecimal -> BigDecimal.TEN)
                                   .isOfType(Integer.class)
                                   .then(integer -> BigDecimal.valueOf(integer))
                                   .isOfType(Float.class)
                                   .then(aFloat -> BigDecimal.valueOf(2.2))
                                   .elseDefault(BigDecimal.ONE);
        Assert.assertEquals(bigDec,
                            BigDecimal.ONE);
    }

    @Test
    public void functionalMatcherPositiveOptionTest() {

        Option<BigDecimal> bigDec = Matcher.caseWhen(40)
                                           .isOfType(BigDecimal.class)
                                           .then(bigDecimal -> BigDecimal.TEN)
                                           .isOfType(Integer.class)
                                           .then(integer -> BigDecimal.valueOf(integer))
                                           .isOfType(Float.class)
                                           .then(aFloat -> BigDecimal.valueOf(2.2))
                                           .yield();
        Assert.assertTrue(bigDec.isPresent());
    }

    @Test
    public void functionalMatcherNegativeOptionTest() {

        Option<BigDecimal> bigDec = Matcher.caseWhen(BigInteger.valueOf(20))
                                           .isOfType(BigDecimal.class)
                                           .then(bigDecimal -> BigDecimal.TEN)
                                           .isOfType(Integer.class)
                                           .then(integer -> BigDecimal.valueOf(integer))
                                           .isOfType(Float.class)
                                           .then(aFloat -> BigDecimal.valueOf(2.2))
                                           .yield();
        Assert.assertFalse(bigDec.isPresent());
    }

    //    @Test
    //    public void functionalMatchIsSameTypeAsPositiveTest() {
    //        String resultStr = Matcher.caseWhen(Tuple2.of("blah",
    //                                              "la"))
    //                          .isSameTypeAs(Tuple2.of(1,
    //                                                  2))
    //                          .then(integerIntegerTuple2 -> integerIntegerTuple2.toString())
    //                          .isSameTypeAs(Tuple2.of("re",
    //                                                  "rah"))
    //                          .then(stringStringTuple2 -> stringStringTuple2.toString())
    //                          .isSameTypeAs(Tuple2.of(12.23f,
    //                                                  1.2f))
    //                          .then(floatFloatTuple2 -> floatFloatTuple2.toString())
    //                          .orElse("No match");
    //        logger.info(HashMap.of("actual", resultStr, "expected", Tuple2.of("blah", "la").toString()).mkString());
    //        Assert.assertEquals(Tuple2.of("blah", "la").toString(), resultStr);
    //    }
    //
    //    @Test
    //    public void functionalMatchIsSameTypeAsNegativeTest() {
    //        String resultStr = Matcher.caseWhen(Tuple2.of("blah",
    //                                                      2))
    //                                  .isSameTypeAs(Tuple2.of(1,
    //                                                          2))
    //                                  .then(integerIntegerTuple2 -> integerIntegerTuple2.toString())
    //                                  .isSameTypeAs(Tuple2.of("re",
    //                                                          "rah"))
    //                                  .then(stringStringTuple2 -> stringStringTuple2.toString())
    //                                  .isSameTypeAs(Tuple2.of(12.23f,
    //                                                          1.2f))
    //                                  .then(floatFloatTuple2 -> floatFloatTuple2.toString())
    //                                  .orElse("No match");
    //        logger.info(HashMap.of("actual", resultStr, "expected", "No match").mkString());
    //        Assert.assertEquals(resultStr, "No match");
    //    }
}