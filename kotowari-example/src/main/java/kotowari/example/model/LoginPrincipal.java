package kotowari.example.model;

import enkan.security.UserPrincipal;

/**
 * @author kawasima
 */
public class LoginPrincipal implements UserPrincipal {
    private String name;

    public LoginPrincipal() {

    }

    public LoginPrincipal(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }
    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean hasPermission(String permission) {
        return false;
    }
}
