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

    /**
     * Interfaces that this middleware mixes into the request or response
     * via {@link enkan.util.MixinUtils#mixin}.
     *
     * <p>Declaring these allows the framework to pre-create request/response
     * objects that already implement all required interfaces, eliminating
     * per-request proxy creation.
     *
     * @return mixin interfaces
     */
    Class<?>[] mixins() default {};
}
