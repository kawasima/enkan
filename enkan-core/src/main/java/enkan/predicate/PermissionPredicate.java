package enkan.predicate;

import enkan.data.PrincipalAvailable;
import enkan.security.UserPrincipal;

import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author kawasima
 */
public class PermissionPredicate<REQ extends PrincipalAvailable> implements PrintablePredicate<REQ> {
    private String permission;

    public PermissionPredicate(String permission) {
        this.permission = permission;
    }

    @Override
    public boolean test(REQ req) {
        return Stream.of(req)
                .filter(PrincipalAvailable.class::isInstance)
                .map(PrincipalAvailable.class::cast)
                .map(PrincipalAvailable::getPrincipal)
                .filter(UserPrincipal.class::isInstance)
                .map(UserPrincipal.class::cast)
                .filter(up -> up.hasPermission(permission))
                .findFirst()
                .isPresent();
    }

    @Override
    public String toString() {
        return "permission = " + permission;
    }
}
