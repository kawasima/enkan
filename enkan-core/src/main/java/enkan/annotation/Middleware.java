package enkan.annotation;

import java.lang.annotation.*;

/**
 * @author kawasima
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Middleware {
    String name();
    String[] dependencies() default "";
}
