package kotowari.inject.parameter;

import enkan.data.Flash;
import enkan.data.HttpRequest;
import kotowari.inject.ParameterInjector;

import java.io.Serializable;

public class FlashInjector<T extends Serializable> implements ParameterInjector<Flash<T>> {
    @Override
    public String getName() {
        return "Flash";
    }

    @Override
    public boolean isApplicable(Class<?> type, HttpRequest request) {
        return Flash.class.isAssignableFrom(type);
    }

    @Override
    public Flash<T> getInjectObject(HttpRequest request) {
        return request.getFlash();
    }
}
