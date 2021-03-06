package enkan.util;

import enkan.data.PrincipalAvailable;
import enkan.data.UriAvailable;
import enkan.predicate.*;

import java.util.function.Predicate;

/**
 * @author kawasima
 */
public class Predicates {
    public static final NonePredicate NONE = new NonePredicate<>();
    public static final AnyPredicate ANY = new AnyPredicate<>();

    private Predicates() {
    }

    @SuppressWarnings("unchecked")
    public static <T> Predicate<? super T> any() {
        return ANY;
    }

    @SuppressWarnings("unchecked")
    public static <T> Predicate<? super T> none() {
        return NONE;
    }

    @SafeVarargs
    public static <T> Predicate<? super T> and(Predicate<T> pred, Predicate<? super T>... preds) {
        for (Predicate<? super T> p : preds) {
            pred = pred.and(p);
        }
        return pred;
    }

    @SafeVarargs
    public static <T> Predicate<? super T> or(Predicate<T> pred, Predicate<? super T>... preds) {
        for (Predicate<? super T> p : preds) {
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

    public static <REQ> EnvPredicate<REQ> envIn(String... envs) {
        return new EnvPredicate<>(envs);
    }
}
