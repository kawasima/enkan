package enkan.security;

import java.security.Principal;

/**
 * @author kawasima
 */
public interface AuthBackend<REQ, T> {
    /**
     * Parse the given request for eliciting an authentication data.
     *
     * @param request
     * @return authenticationData
     */
    T parse(REQ request);

    /**
     * Authenticate the given request.
     *
     * @param request
     * @param authenticationData
     * @return
     */
    Principal authenticate(REQ request, T authenticationData);
}
