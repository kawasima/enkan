package enkan.security.backend;

import enkan.data.HttpRequest;
import enkan.security.AuthBackend;
import enkan.util.ThreadingUtils;

import java.security.Principal;

/**
 * @author kawasima
 */
public class TokenBackend implements AuthBackend<HttpRequest, String> {
    private String tokenName = "Token";

    protected String parseAuthorizationHeader(HttpRequest request, String tokenName) {
        return ThreadingUtils.some(request.getHeaders().get("Authorization"),
                auth -> auth.replace("^" + tokenName + " (.+)$", "$1")).orElse(null);
    }

    @Override
    public String parse(HttpRequest request) {
        return parseAuthorizationHeader(request, tokenName);
    }

    @Override
    public Principal authenticate(HttpRequest request, String token) {
        return null;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }
}
