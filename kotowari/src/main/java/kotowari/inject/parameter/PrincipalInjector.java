package kotowari.inject.parameter;

import enkan.data.HttpRequest;
import kotowari.inject.ParameterInjector;

import java.security.Principal;

public class PrincipalInjector implements ParameterInjector<Principal> {
    @Override
    public String getName() {
        return "Principal";
    }

    @Override
    public boolean isApplicable(Class<?> type, HttpRequest request) {
        return Principal.class.isAssignableFrom(type);
    }

    @Override
    public Principal getInjectObject(HttpRequest request) {
        if (request != null) {
            return request.getPrincipal();
        }
        return null;
    }
}
