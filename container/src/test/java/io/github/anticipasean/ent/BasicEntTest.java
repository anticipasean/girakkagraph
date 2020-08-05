package io.github.anticipasean.ent;

import cyclops.control.Option;
import cyclops.data.Seq;
import cyclops.data.tuple.Tuple2;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

public class BasicEntTest {

    private Logger logger = LoggerFactory.getLogger(BasicEntTest.class);

    @Test
    public void mapValueTest() {
        Assert.assertEquals(Ent.of("one",
                                   1)
                               .map(integer -> 2),
                            Ent.of("one",
                                   2));
    }

    @Test
    public void mapKeyTest() {
        Assert.assertEquals(Ent.of("one",
                                   1)
                               .mapKeys(s -> "two"),
                            Ent.of("two",
                                   1));
    }

    @Test
    public void headTest() {
        Ent.of("one",
               1);
    }

    @Test
    public void primitiveMatchGetTest() {
        Assert.assertEquals(Ent.of("blah",
                                   2)
                               .matchGetValue("blah",
                                              matcher -> matcher.caseWhenValue()
                                                                .isOfType(BigDecimal.class)
                                                                .then(bigDecimal -> "one_big_dec")
                                                                .isOfType(Float.class)
                                                                .then(aFloat -> "two_float")
                                                                .isOfType(Integer.class)
                                                                .then(integer -> "two_int")
                                                                .elseDefault("no match"))
                               .orElse("key not found"),
                            "two_int");
    }

    @Test
    public void matchFoldTest() {
        Seq<Tuple2<String, Number>> tuples = Seq.of("one",
                                                    "one-half",
                                                    "two-and-a-half",
                                                    "ten")
                                                .zip(Seq.of(1,
                                                            1.5f,
                                                            2.5f,
                                                            10));
        Ent<String, Number> numberEnt = Ent.fromIterable(tuples);
        Integer sumOfIntegers = numberEnt.matchFold(0,
                                                    matcher -> matcher.caseWhenKeyValue()
                                                                      .valueOfType(Integer.class)
                                                                      .then((s, integer) -> Tuple2.of(String.valueOf(integer),
                                                                                                      integer))
                                                                      .elseDefault(Tuple2.of("0",
                                                                                             0)),
                                                    stringIntegerTuple2 -> stringIntegerTuple2._2(),
                                                    (integer, integer2) -> integer + integer2);
        Assert.assertEquals(sumOfIntegers.intValue(),
                            11);
    }

    @Test
    public void matchFoldOptionTest() {
        Seq<Tuple2<String, Number>> tuples = Seq.of("one",
                                                    "one-and-a-half",
                                                    "two-and-a-half",
                                                    "ten")
                                                .zip(Seq.of(1,
                                                            1.5f,
                                                            2.5f,
                                                            10));
        Ent<String, Number> numberEnt = Ent.fromIterable(tuples);
        Option<Integer> integerSumOpt = numberEnt.matchFold(Option.<Integer>none(),
                                                            matcher -> matcher.caseWhenKeyValue()
                                                                              .valueOfType(Integer.class)
                                                                              .then((s, integer) -> Tuple2.of(String.valueOf(integer),
                                                                                                              Option.of(integer)))
                                                                              .elseDefault(null,
                                                                                           Option.some(0)),
                                                            stringIntegerOptTuple2 -> stringIntegerOptTuple2._2(),
                                                            (integerOpt1, integerOpt2) -> integerOpt2.map(integer2 -> integerOpt1.map(integer1 ->
                                                                                                                                          integer2
                                                                                                                                              + integer1)
                                                                                                                                 .orElse(integer2)));
        Assert.assertEquals((int) (integerSumOpt.isPresent() ? integerSumOpt.orElse(null) : -1),
                            11);
    }

    @Test
    public void matchFoldValuesTest() {
        Seq<Tuple2<String, Number>> tuples = Seq.of("one",
                                                    "one-and-a-half",
                                                    "two-and-a-half",
                                                    "ten")
                                                .zip(Seq.of(1,
                                                            1.5f,
                                                            2.5f,
                                                            10));
        Ent<String, Number> numberEnt = Ent.fromIterable(tuples);
        Integer integerSum = numberEnt.matchFoldValues(0,
                                                       matcher -> matcher.caseWhenValue()
                                                                         .isOfType(Integer.class)
                                                                         .then(integer -> integer)
                                                                         .elseDefault(0),
                                                       (integer, integer2) -> integer + integer2);
        Assert.assertEquals(integerSum.intValue(),
                            11);
    }

}
