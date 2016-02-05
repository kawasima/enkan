package kotowari.middleware;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.data.Routable;
import enkan.middleware.AbstractWebMiddleware;
import enkan.util.MixinUtils;
import kotowari.data.FormAvailable;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

import static enkan.util.ReflectionUtils.tryReflection;

/**
 * @author kawasima
 */
@Middleware(name = "form", dependencies = {"params", "routing"})
public class FormMiddleware extends AbstractWebMiddleware {
    // FIXME this must be provided from components.
    private ObjectMapper mapper;

    public FormMiddleware() {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS, false);
    }

    protected <T extends Serializable> T createForm(Class<T> formClass, Map<String, ?> params) {
        return tryReflection(() -> mapper.convertValue(params, formClass));
    }

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain next) {
        Method method = ((Routable) request).getControllerMethod();
        request = MixinUtils.mixin(request, FormAvailable.class);
        for (Parameter parameter : method.getParameters()) {
            Class<?> type = parameter.getType();
            if (HttpRequest.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type)) {
                continue;
            }
            FormAvailable.class.cast(request)
                    .setForm(createForm((Class<? extends Serializable>) type, request.getParams()));
        }

        return castToHttpResponse(next.next(request));
    }
}
