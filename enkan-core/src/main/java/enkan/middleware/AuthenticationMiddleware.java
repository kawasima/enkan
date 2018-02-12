package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.data.PrincipalAvailable;
import enkan.security.AuthBackend;
import enkan.util.MixinUtils;
import enkan.util.ThreadingUtils;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

/**
 * The middleware for authentication.
 *
 * @author kawasima
 */
@Middleware(name = "authentication")
public class AuthenticationMiddleware<REQ, RES, T> implements enkan.Middleware<REQ, RES, REQ, RES> {
    private final List<AuthBackend<REQ, T>> backends;

    public AuthenticationMiddleware(List<AuthBackend<REQ, T>> backends) {
        this.backends = backends;
    }

    @Override
    public RES handle(REQ req, MiddlewareChain<REQ, RES, ?, ?> next) {
        final REQ request = MixinUtils.mixin(req, PrincipalAvailable.class);
        for (AuthBackend<REQ, T> backend : backends) {
            Optional<Principal> principal = ThreadingUtils.some(
                    backend.parse(request),
                    data -> backend.authenticate(request, data));
            principal.ifPresent(((PrincipalAvailable) request)::setPrincipal);
        }
        return next.next(request);
    }
}
