package io.github.anticipasean.ent;

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


}
