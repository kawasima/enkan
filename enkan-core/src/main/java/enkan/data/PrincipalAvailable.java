package enkan.data;

import java.security.Principal;

/**
 * @author kawasima
 */
public interface PrincipalAvailable extends Extendable {
    default Principal getPrincipal() {
        return (Principal) getExtension("principal");
    }

    default void setPrincipal(Principal principal) {
        setExtension("principal", principal);
    }
}
