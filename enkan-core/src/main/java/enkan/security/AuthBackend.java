package enkan.security;

import java.security.Principal;

/**
 * @author kawasima
 */
public interface AuthBackend<REQ, T> {
    /**
     * Parse a request
     *
     * @param request
     * @return authenticationData
     */
    T parse(REQ request);

    /**
     * Authenticate request
     *
     * @param request
     * @param authenticationData
     * @return
     */
    Principal authenticate(REQ request, T authenticationData);
}
