package enkan.predicate;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * A {@link Predicate} extension whose composed predicates produce
 * human-readable {@code toString()} output.
 *
 * <p>The standard {@link Predicate#and}, {@link Predicate#or} and
 * {@link Predicate#negate} combinators return anonymous lambdas whose
 * {@code toString()} is an opaque JVM-generated identifier.
 * {@code PrintablePredicate} overrides all three combinators to return
 * new {@code PrintablePredicate} instances that render the expression in
 * readable infix notation:
 * <ul>
 *   <li>{@code and}    → {@code "p1 && p2"}</li>
 *   <li>{@code or}     → {@code "(p1) || (p2)"}</li>
 *   <li>{@code negate} → {@code "!p"}</li>
 * </ul>
 *
 * <p>This makes middleware-stack diagnostics significantly easier, because
 * the predicates controlling which middleware is applied to a given request
 * can be printed in a legible form.
 *
 * @param <T> the type of object tested by this predicate
 * @author kawasima
 */
public interface PrintablePredicate<T> extends Predicate<T> {
    default Predicate<T> and(Predicate<? super T> other) {
        Objects.requireNonNull(other);
        Predicate<T> orig = this;
        return new PrintablePredicate<>() {
            @Override
            public boolean test(T t) {
                return orig.test(t) && other.test(t);
            }

            @Override
            public String toString() {
                return orig + " && " + other;
            }
        };
    }

    default Predicate<T> or(Predicate<? super T> other) {
        Objects.requireNonNull(other);
        Predicate<T> orig = this;
        return new PrintablePredicate<>() {
            @Override
            public boolean test(T t) {
                return orig.test(t) || other.test(t);
            }

            @Override
            public String toString() {
                return "(" + orig + ") || (" + other + ")";
            }
        };
    }

    @Override
    default Predicate<T> negate() {
        Predicate<T> orig = this;
        return new PrintablePredicate<>() {
            @Override
            public boolean test(T s) {
                return !orig.test(s);
            }

            @Override
            public String toString() {
                return "!" + orig;
            }
        };
    }
}
