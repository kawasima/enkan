package enkan.predicate;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author kawasima
 */
public interface PrintablePredicate<T> extends Predicate<T> {
    default Predicate<T> and(Predicate<? super T> other) {
        Objects.requireNonNull(other);
        Predicate<T> orig = this;
        return new PrintablePredicate<T>() {
            @Override
            public boolean test(T t) {
                return orig.test(t) && other.test(t);
            }

            @Override
            public String toString() {
                return orig.toString() + " && " + other.toString();
            }
        };
    }

    default Predicate<T> or(Predicate<? super T> other) {
        Objects.requireNonNull(other);
        Predicate<T> orig = this;
        return new PrintablePredicate<T>() {
            @Override
            public boolean test(T t) {
                return orig.test(t) || other.test(t);
            }

            @Override
            public String toString() {
                return "(" + orig.toString() + ") || (" + other.toString() + ")";
            }
        };
    }

    @Override
    default Predicate<T> negate() {
        Predicate<T> orig = this;
        return new PrintablePredicate<T>() {
            @Override
            public boolean test(T s) {
                return !orig.test(s);
            }

            @Override
            public String toString() {
                return "!" + orig.toString();
            }
        };
    }
}
