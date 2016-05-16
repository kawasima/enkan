package enkan.util;

import enkan.data.PrincipalAvailable;
import enkan.data.UriAvailable;
import enkan.predicate.*;

import java.util.function.Predicate;

/**
 * @author kawasima
 */
public class Predicates {
    public static final NonePredicate NONE = new NonePredicate();
    public static final AnyPredicate ANY = new AnyPredicate<>();

    private Predicates() {
    }

    public static Predicate and(Predicate pred, Predicate... preds) {
        for (Predicate p : preds) {
            pred = pred.and(p);
        }
        return pred;
    }

    public static Predicate or(Predicate pred, Predicate... preds) {
        for (Predicate p : preds) {
            pred = pred.or(p);
        }
        return pred;
    }

    public static <REQ extends UriAvailable> PathPredicate<REQ> path(String path) {
        return PathPredicate.ANY(path);
    }

    public static <REQ extends PrincipalAvailable> PermissionPredicate<REQ> permission(String permission) {
        return new PermissionPredicate<>(permission);
    }

    public static <REQ extends PrincipalAvailable> AuthenticatedPredicate<REQ> authenticated() {
        return new AuthenticatedPredicate<>();
    }

    public static EnvPredicate envIn(String... envs) {
        return new EnvPredicate(envs);
    }
}
