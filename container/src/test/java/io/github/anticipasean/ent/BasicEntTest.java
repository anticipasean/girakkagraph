package io.github.anticipasean.ent;

import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

public class BasicEntTest {

    private Logger logger = LoggerFactory.getLogger(BasicEntTest.class);

    //    @Test
    //    public void mapperTest(){
    //        Ent.of("one", 1).headOption();
    //    }
    //
    //    @Test
    //    public void headTest(){
    //        Ent.of("one", 1).headOption();
    //    }
    //
    @Test
    public void primitivesGetAndMatchTest() {
        logger.info("constructing an ent");
        Assert.assertEquals("two_int",
                            Ent.of("blah",
                                   2)
                               .matchGet("blah",
                                         matcher -> matcher.caseWhenValue()
                                                           .isOfType(BigDecimal.class)
                                                           .then(bigDecimal -> "one_big_dec")
                                                           .isOfType(Float.class)
                                                           .then(aFloat -> "two_float")
                                                           .isOfType(Integer.class)
                                                           .then(integer -> "two_int")
                                                           .elseDefault("no match"))
                               .orElse("key not found"));
    }
    //
    //    @Test
    //    public void mapWithPatternTest() {
    //        Ent.of("blah",
    //               2)
    //           .matchMap(integerIfMatchClause -> integerIfMatchClause.ifOfType(String.class)
    //                                                                 .then(s -> "twoStr")
    //                                                                 .orElse("notStr"))
    //           .forEach(stringStringTuple2 -> System.out.println(
    //               "tuple: " + stringStringTuple2._1() + " " + stringStringTuple2._2()));
    //    }

}
