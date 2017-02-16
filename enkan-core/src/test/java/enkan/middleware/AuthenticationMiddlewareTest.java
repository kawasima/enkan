package enkan.middleware;

import enkan.chain.DefaultMiddlewareChain;
import enkan.data.PrincipalAvailable;
import enkan.security.AuthBackend;
import enkan.security.UserPrincipal;
import enkan.util.Predicates;
import org.junit.Test;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @kawasima
 */
public class AuthenticationMiddlewareTest {
    @Test
    public void test() {
        AuthenticationMiddleware middleware = new AuthenticationMiddleware(Collections.singletonList(createAuthBackend()));
        Request request = new Request("kawasima");
        middleware.handle(request, new DefaultMiddlewareChain<>(Predicates.ANY, "",
                (req, chain) -> "ok"));
        assertNotNull(request.getPrincipal());
    }

    private class Request implements PrincipalAvailable {
        private final Map<String ,Object> extensions = new HashMap<>();
        private final String name;

        public Request(String name) {
            this.name = name;
        }
        @Override
        public Object getExtension(String name) {
            return extensions.get(name);
        }

        @Override
        public void setExtension(String name, Object extension) {
            extensions.put(name, extension);
        }
        public String getName() {
            return name;
        }
    }

    private AuthBackend<Request, String> createAuthBackend() {
        return new AuthBackend<Request, String>() {
            @Override
            public String parse(Request request) {
                return request.getName();
            }

            @Override
            public Principal authenticate(Request request, String authenticationData) {
                return new UserPrincipal() {
                    @Override
                    public String getName() {
                        return authenticationData;
                    }

                    @Override
                    public boolean hasPermission(String permission) {
                        return false;
                    }
                };
            }
        };
    }
}
