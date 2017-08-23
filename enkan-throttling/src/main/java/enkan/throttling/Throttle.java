package enkan.throttling;

import enkan.data.HttpRequest;

import java.util.function.Function;

/**
 * @author kawasima
 */
public class Throttle {
    private Function<HttpRequest, ?> discriminateFn;
    private String name;

    public Throttle(String name, Function<HttpRequest, Boolean> discriminateFn) {
        this.name = name;
        this.discriminateFn = discriminateFn;
    }

    public boolean apply(HttpRequest request) {
        Object discriminator = discriminateFn.apply(request);
        if (discriminator == null) return false;
        return true;
    }
}
