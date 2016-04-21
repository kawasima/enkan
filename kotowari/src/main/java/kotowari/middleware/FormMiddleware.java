package kotowari.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.component.BeansConverter;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.data.Routable;
import enkan.data.Session;
import enkan.middleware.AbstractWebMiddleware;
import enkan.security.UserPrincipal;
import enkan.util.MixinUtils;
import kotowari.data.BodyDeserializable;

import javax.inject.Inject;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

/**
 * @author kawasima
 */
@Middleware(name = "form", dependencies = {"params", "routing"})
public class FormMiddleware extends AbstractWebMiddleware {
    @Inject
    protected BeansConverter beans;

    protected <T extends Serializable> T createForm(Class<T> formClass, Map<String, ?> params) {
        return beans.createFrom(params, formClass);
    }

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain next) {
        Method method = ((Routable) request).getControllerMethod();
        request = MixinUtils.mixin(request, BodyDeserializable.class);
        for (Parameter parameter : method.getParameters()) {
            Class<?> type = parameter.getType();
            if (HttpRequest.class.isAssignableFrom(type)
                    || Session.class.isAssignableFrom(type)
                    || UserPrincipal.class.isAssignableFrom(type)
                    || Map.class.isAssignableFrom(type)) {
                continue;
            }
            BodyDeserializable.class.cast(request)
                    .setDeserializedBody(createForm((Class<? extends Serializable>) type, request.getParams()));
        }

        return castToHttpResponse(next.next(request));
    }
}
