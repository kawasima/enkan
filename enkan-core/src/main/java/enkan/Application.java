package enkan;

import enkan.exception.MisconfigurationException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The application applied middlewares.
 *
 * @author kawasima
 */
public interface Application<REQ, RES> {
    <M_REQ, M_RES> void use(Middleware<M_REQ, M_RES> middleware);

    <IN, OUT> void use(Decision<IN> decision, Middleware<IN, OUT> middleware);

    void clear();
    RES handle(REQ req);

    default void validate() {
        Set<String> priorMiddelwares = new HashSet<>();
        priorMiddelwares.add("");

        for (Middleware middleware : getMiddlewareStack()) {
            enkan.annotation.Middleware anno = middleware.getClass().getAnnotation(enkan.annotation.Middleware.class);
            if (anno == null) continue;
            String name = anno.name();
            String[] dependencies = anno.dependencies();
            Set<String> lackMiddlewares = Arrays.stream(dependencies)
                    .filter(d -> !priorMiddelwares.contains(d))
                    .collect(Collectors.toSet());

            if (!lackMiddlewares.isEmpty()) {
                throw MisconfigurationException.raise("MIDDLEWARE_DEPENDENCY", name, lackMiddlewares);
            }
            if (!priorMiddelwares.add(name)) {
                throw MisconfigurationException.raise("DUPLICATE_MIDDLEWARES", name);
            }
        }
    }

    List<Middleware> getMiddlewareStack();
}
