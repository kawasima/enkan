package enkan.predicate;

import java.util.function.Predicate;

/**
 * @author kawasima
 */
public class AnyPredicate<REQ> implements Predicate<REQ> {
    @Override
    public boolean test(REQ req) {
        return true;
    }

    @Override
    public String toString() {
        return "ANY";
    }
}
