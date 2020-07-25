package io.github.anticipasean.ent;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
//    @Test
//    public void primitivesGetAndMatchTest() {
//        logger.info("constructing an ent");
//        Assertions.assertEquals("two",
//                                Ent.of("blah",
//                                       2)
//                                   .matchGet("blah",
//                                                integerIfMatchClause -> integerIfMatchClause.ifOfType(Integer.class)
//                                                                                            .and(integer -> integer == 1)
//                                                                                            .then(integer -> "one")
//                                                                                            .ifOfType(Integer.class)
//                                                                                            .and(integer -> integer == 2)
//                                                                                            .then(integer -> "two")
//                                                                                            .orElse("no match"))
//                                   .orElse("no blah value"));
//    }
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
