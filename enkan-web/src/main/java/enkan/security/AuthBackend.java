package enkan.security;

import enkan.data.HttpRequest;

/**
 * @author kawasima
 */
public interface AuthBackend {
    void parse(HttpRequest request);
    void authenticate(HttpRequest request);
}
