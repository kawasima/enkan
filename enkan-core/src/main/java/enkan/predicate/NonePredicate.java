package enkan.predicate;

import java.util.function.Predicate;

/**
 * @author kawasima
 */
public class NonePredicate<REQ> implements Predicate<REQ> {
    @Override
    public boolean test(REQ req) {
        return false;
    }

    @Override
    public String toString() {
        return "NONE";
    }
}
