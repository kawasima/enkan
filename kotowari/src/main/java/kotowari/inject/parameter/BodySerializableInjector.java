package kotowari.inject.parameter;

import enkan.data.HttpRequest;
import kotowari.data.BodyDeserializable;
import kotowari.inject.ParameterInjector;

public class BodySerializableInjector<T> implements ParameterInjector<T> {
    @Override
    public String getName() {
        return "Serializable Object";
    }

    @Override
    public boolean isApplicable(Class<?> type, HttpRequest request) {
        if (request instanceof BodyDeserializable) {
            Object bodyObj = BodyDeserializable.class.cast(request).getDeserializedBody();
            return bodyObj == null || (bodyObj != null && type.isAssignableFrom(bodyObj.getClass()));
        }
        return false;
    }

    @Override
    public T getInjectObject(HttpRequest request) {
        return BodyDeserializable.class.cast(request).getDeserializedBody();
    }
}
