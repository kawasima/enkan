package enkan.security;

import java.io.Serializable;
import java.security.Principal;

/**
 * @author kawasima
 */
public interface UserPrincipal extends Principal, Serializable
{
    boolean hasPermission(String permission);
}
