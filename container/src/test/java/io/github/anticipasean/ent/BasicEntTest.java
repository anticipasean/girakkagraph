package io.github.anticipasean.ent;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicEntTest {

    private Logger logger = LoggerFactory.getLogger(BasicEntTest.class);

    @Test
    public void primitivesGetAndMatchTest() {
        logger.info("constructing an ent");
        Assertions.assertEquals("two",
                                Ent.of("blah",
                                       2)
                                   .getAndMatch("blah",
                                                integerIfMatchClause -> integerIfMatchClause.ifOfType(Integer.class)
                                                                                            .and(integer -> integer == 1)
                                                                                            .then(integer -> "one")
                                                                                            .ifOfType(Integer.class)
                                                                                            .and(integer -> integer == 2)
                                                                                            .then(integer -> "two")
                                                                                            .orElse("no match"))
                                   .orElse("no blah value"));
    }

}
