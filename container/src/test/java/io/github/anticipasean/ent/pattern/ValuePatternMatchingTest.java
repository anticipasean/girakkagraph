package io.github.anticipasean.ent.pattern;

import cyclops.control.Option;
import java.math.BigDecimal;
import java.math.BigInteger;
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
    public void typeMatchPositiveTest() {

        BigDecimal bigDec = Matcher.caseWhen(40)
                                   .isOfType(BigDecimal.class)
                                   .then(bigDecimal -> BigDecimal.TEN)
                                   .isOfType(Integer.class)
                                   .then(integer -> BigDecimal.valueOf(integer))
                                   .isOfType(Float.class)
                                   .then(aFloat -> BigDecimal.valueOf(2.2))
                                   .elseDefault(BigDecimal.ONE);
        Assert.assertEquals(bigDec,
                            BigDecimal.valueOf(40), "type was supposed to have a match");
    }

    @Test
    public void typeMatchNegativeTest() {

        BigDecimal bigDec = Matcher.caseWhen(BigInteger.valueOf(20))
                                   .isOfType(BigDecimal.class)
                                   .then(bigDecimal -> BigDecimal.TEN)
                                   .isOfType(Integer.class)
                                   .then(integer -> BigDecimal.valueOf(integer))
                                   .isOfType(Float.class)
                                   .then(aFloat -> BigDecimal.valueOf(2.2))
                                   .elseDefault(BigDecimal.ONE);
        Assert.assertEquals(bigDec,
                            BigDecimal.ONE, "type was not supposed to have a match and use default instead");
    }

    @Test
    public void typeMatchPositiveResultOptionTest() {

        Option<BigDecimal> bigDec = Matcher.caseWhen(40)
                                           .isOfType(BigDecimal.class)
                                           .then(bigDecimal -> BigDecimal.TEN)
                                           .isOfType(Integer.class)
                                           .then(integer -> BigDecimal.valueOf(integer))
                                           .isOfType(Float.class)
                                           .then(aFloat -> BigDecimal.valueOf(2.2))
                                           .elseOption();
        Assert.assertTrue(bigDec.isPresent());
    }

    @Test
    public void typeMatchNegativeResultOptionTest() {

        Option<BigDecimal> bigDec = Matcher.caseWhen(BigInteger.valueOf(20))
                                           .isOfType(BigDecimal.class)
                                           .then(bigDecimal -> BigDecimal.TEN)
                                           .isOfType(Integer.class)
                                           .then(BigDecimal::valueOf)
                                           .isOfType(Float.class)
                                           .then(aFloat -> BigDecimal.valueOf(2.2))
                                           .elseOption();
        Assert.assertFalse(bigDec.isPresent(), "no result option was expected");
    }

    @Test
    public void optionTypeMatchPositiveTest() {
        Option<Number> numberOpt = Matcher.caseWhen(Option.of(40))
                                          .isOfType(BigDecimal.class)
                                          .then(bigDecimal -> (Number) BigDecimal.TEN)
                                          .isOfType(Integer.class)
                                          .then(integer -> 32)
                                          .isOptionOfType(Integer.class)
                                          .then(intOpt -> intOpt.map(i -> i + 2)
                                                                .orElse(-1))
                                          .isOfType(Float.class)
                                          .then(aFloat -> 2.2f)
                                          .isOptionOfType(BigDecimal.class)
                                          .then(bigDecOpt -> bigDecOpt.orElse(BigDecimal.ZERO))
                                          .elseOption();
        Assert.assertTrue(numberOpt.isPresent() && numberOpt.orElse(-3)
                                                            .intValue() == 42,
                          "number option return value does not match option of 42: [ actual: " + numberOpt.mkString() + " ]");
    }

    @Test
    public void optionTypeMatchNegativeTest() {
        Option<Number> numberOpt = Matcher.caseWhen(Option.of(40))
                                          .isOfType(BigDecimal.class)
                                          .then(bigDecimal -> (Number) BigDecimal.TEN)
                                          .isOfType(Integer.class)
                                          .then(integer -> 32)
                                          .isOfType(Float.class)
                                          .then(aFloat -> 2.2f)
                                          .isOptionOfType(BigDecimal.class)
                                          .then(bigDecOpt -> bigDecOpt.orElse(BigDecimal.ZERO))
                                          .elseOption();
        Assert.assertFalse(numberOpt.isPresent(),
                           "number option not expected to return a value: [ actual: " + numberOpt.mkString() + " ]");
    }

    @Test
    public void optionTypeMatchAsFirstClausePositiveTest() {
        Option<Number> numberOpt = Matcher.caseWhen(Option.of(40))
                                          .isOptionOfType(Integer.class)
                                          .then(intOpt -> (Number) intOpt.map(i -> i + 2)
                                                                .orElse(-1))
                                          .isOfType(BigDecimal.class)
                                          .then(bigDecimal -> (Number) BigDecimal.TEN)
                                          .isOfType(Integer.class)
                                          .then(integer -> 32)
                                          .isOfType(Float.class)
                                          .then(aFloat -> 2.2f)
                                          .isOptionOfType(BigDecimal.class)
                                          .then(bigDecOpt -> bigDecOpt.orElse(BigDecimal.ZERO))
                                          .elseOption();
        Assert.assertTrue(numberOpt.isPresent() && numberOpt.orElse(-3)
                                                            .intValue() == 42,
                          "number option return value does not match option of 42: [ actual: " + numberOpt.mkString() + " ]");
    }

    @Test
    public void optionTypeMatchAsFirstClauseNegativeTest() {
        Option<Number> numberOpt = Matcher.caseWhen(Option.of(40))
                                          .isOptionOfType(BigDecimal.class)
                                          .then(bigDecOpt -> (Number) bigDecOpt.orElse(BigDecimal.ZERO))
                                          .isOptionOfType(Float.class)
                                          .then(floatOption -> floatOption.orElse(1.1f))
                                          .isOfType(Integer.class)
                                          .then(integer -> 32)
                                          .isOfType(Float.class)
                                          .then(aFloat -> 2.2f)
                                          .elseOption();
        Assert.assertFalse(numberOpt.isPresent(),
                           "number option not expected to return a value: [ actual: " + numberOpt.mkString() + " ]");
    }

}