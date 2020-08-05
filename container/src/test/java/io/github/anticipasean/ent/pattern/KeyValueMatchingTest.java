package io.github.anticipasean.ent.pattern;

import cyclops.companion.Streamable;
import cyclops.data.Seq;
import cyclops.data.tuple.Tuple2;
import cyclops.reactive.ReactiveSeq;
import io.github.anticipasean.ent.Ent;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

public class KeyValueMatchingTest {

    private Logger logger = LoggerFactory.getLogger(KeyValueMatchingTest.class);

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
                                                              .then(Function.identity(),
                                                                    bigDecimalForm -> "5.0000")
                                                              .valueOfTypeAndBothFit(Integer.class,
                                                                                     (s, i) -> "five".equalsIgnoreCase(s)
                                                                                         && 5 == i)
                                                              .then(Function.identity(),
                                                                    (integerForm) -> "5")
                                                              .elseDefault(Tuple2.of("No match",
                                                                                     "No match"));
        //        logger.info("pattern_matching_result: [ value expected: {} as {}, actual: {} as {} ]",
        //                    "5",
        //                    String.class,
        //                    patternMatchingResult._2(),
        //                    patternMatchingResult._2()
        //                                         .getClass());
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
        //        logger.info("pattern_matching_result: [ expected: {} as {}, actual: {} as {} ]",
        //                    Tuple2.of("five",
        //                              "5"),
        //                    Tuple2.class,
        //                    patternMatchingResult,
        //                    patternMatchingResult.getClass());
        Assert.assertEquals(patternMatchingResult,
                            Tuple2.of("five",
                                      "5"));


    }

    @Test
    public void matchConcatTest() {
        Seq<Tuple2<Integer, List<Integer>>> tuples = Seq.of(Tuple2.of(12,
                                                                      Arrays.asList(1,
                                                                                    2,
                                                                                    3)),
                                                            Tuple2.of(13,
                                                                      Arrays.asList(4,
                                                                                    5,
                                                                                    6)));

        Ent<Integer, Integer> ent = Ent.fromIterable(tuples)
                                       .matchConcatMap(matcher -> matcher.caseWhenKeyValue()
                                                                         .keyFits(integer -> integer > 10)
                                                                         .then((integer, integers) -> Tuple2.of((Iterable<Integer>) integers,
                                                                                                                (Iterable<Integer>) ReactiveSeq.of(integer)
                                                                                                                                               .cycle(integers.size())))
                                                                         .elseDefault(Tuple2.of(Streamable.of(0),
                                                                                                Streamable.of(0))));
        Assert.assertEquals(ent.size(),
                            6,
                            "six pairs of ints not generated: " + ent.mkString());
    }


    @Test
    public void matchPutAllTest() {
        Seq<Tuple2<String, Number>> tuples = Seq.of("one",
                                                    "two_float")
                                                .zip(Seq.of(1,
                                                            2.0F));
        Ent<String, Number> numEnt = Ent.fromIterable(tuples);

        Seq<Tuple2<String, Number>> tuples2 = Seq.of("big_int_one",
                                                     "sixty-three_plus",
                                                     "big_three_22")
                                                 .zip(Seq.of(BigInteger.valueOf(1231313112323L),
                                                             63.2F,
                                                             BigDecimal.valueOf(3.22)));
        Assert.assertEquals(numEnt.size(),
                            2);
        numEnt = numEnt.matchPutAll(tuples2,
                                    matcher -> matcher.caseWhenKeyValue()
                                                      .valueOfType(Integer.class)
                                                      .then((s, integer) -> Tuple2.of(String.valueOf(integer),
                                                                                      (Number) integer))
                                                      .valueOfType(Float.class)
                                                      .then((s, aFloat) -> Tuple2.of(String.valueOf(aFloat),
                                                                                     aFloat))
                                                      .valueOfType(BigDecimal.class)
                                                      .then((s, bigDecimal) -> Tuple2.of(String.valueOf(bigDecimal),
                                                                                         bigDecimal))
                                                      .elseDefault(null));
        Assert.assertEquals(numEnt.size(),
                            4); // minus the BigInteger which was not covered as a possible mapping and no default value provided for it
    }

}
