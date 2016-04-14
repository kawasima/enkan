package enkan.predicate;

/**
 * @author kawasima
 */
public class NonePredicate<REQ> implements PrintablePredicate<REQ> {
    @Override
    public boolean test(REQ req) {
        return false;
    }

    @Override
    public String toString() {
        return "NONE";
    }
}
