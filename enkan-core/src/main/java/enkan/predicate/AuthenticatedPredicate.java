package enkan.predicate;

import enkan.data.PrincipalAvailable;

import java.util.stream.Stream;

/**
 * If a request is authenticated by AuthenticationMiddleware, return true.
 *
 * @author kawasima
 */
public class AuthenticatedPredicate<REQ> implements PrintablePredicate<REQ> {
    @Override
    public boolean test(REQ req) {
        return Stream.of(req)
                .filter(PrincipalAvailable.class::isInstance)
                .map(PrincipalAvailable.class::cast)
                .anyMatch(p -> p.getPrincipal() != null);
    }

    @Override
    public String toString() {
        return "authenticated?";
    }
}
