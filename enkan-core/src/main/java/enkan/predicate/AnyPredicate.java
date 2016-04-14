package enkan.predicate;

/**
 * @author kawasima
 */
public class AnyPredicate<REQ> implements PrintablePredicate<REQ> {
    @Override
    public boolean test(REQ req) {
        return true;
    }

    @Override
    public String toString() {
        return "ANY";
    }
}
