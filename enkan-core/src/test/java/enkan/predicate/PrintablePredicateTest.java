package enkan.predicate;

import org.junit.Test;

import static enkan.util.Predicates.*;
import static org.junit.Assert.*;

/**
 * @author kawasima
 */
public class PrintablePredicateTest {
    @Test
    public void testToString() {
        String pred = new AnyPredicate().and(new AnyPredicate<>().negate()).toString();
        assertEquals("ANY && !ANY", pred);
    }

    @Test
    public void andMethod() {
        String pred = and(new NonePredicate<>(), PathPredicate.GET("^/hoge")).toString();
        assertEquals("NONE && method = GET && path = ^/hoge", pred);
    }

    @Test
    public void orMethod() {
        String pred = or(new NonePredicate<>(), PathPredicate.GET("^/hoge")).toString();
        assertEquals("(NONE) || (method = GET && path = ^/hoge)", pred);
    }

}