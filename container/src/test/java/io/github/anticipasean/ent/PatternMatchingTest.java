package io.github.anticipasean.ent;

import io.github.anticipasean.ent.pattern.PatternMatching;
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

}
