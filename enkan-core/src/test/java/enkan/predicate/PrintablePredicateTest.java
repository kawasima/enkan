package enkan.predicate;

import org.junit.jupiter.api.Test;

import static enkan.util.Predicates.*;
import static org.assertj.core.api.Assertions.*;

/**
 * @author kawasima
 */
public class PrintablePredicateTest {
    @Test
    public void testToString() {
        assertThat(new AnyPredicate<>().and(new AnyPredicate<>().negate()).toString())
                .isEqualTo("ANY && !ANY");
    }

    @Test
    public void andMethod() {
        assertThat(and(new NonePredicate<>(), PathPredicate.GET("^/hoge")).toString())
                .isEqualTo("NONE && method = GET && path = ^/hoge");
    }

    @Test
    public void orMethod() {
        assertThat(or(new NonePredicate<>(), PathPredicate.GET("^/hoge")).toString())
                .isEqualTo("(NONE) || (method = GET && path = ^/hoge)");
    }
}
