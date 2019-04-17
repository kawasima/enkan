package enkan.annotation;

import java.lang.annotation.*;

/**
 * Marks a middleware.
 *
 * @author kawasima
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Middleware {
    /**
     * A name of middleware.
     *
     * @return A name of middleware
     */
    String name();

    /**
     * Dependencies.
     *
     * @return dependencies
     */
    String[] dependencies() default "";
}
