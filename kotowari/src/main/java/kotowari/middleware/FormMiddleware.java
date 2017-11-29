package kotowari.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.component.BeansConverter;
import enkan.data.*;
import enkan.exception.MisconfigurationException;
import enkan.exception.UnreachableException;
import enkan.middleware.AbstractWebMiddleware;
import enkan.security.UserPrincipal;
import enkan.util.MixinUtils;
import kotowari.data.BodyDeserializable;
import kotowari.util.ParameterUtils;

import javax.enterprise.context.Conversation;
import javax.inject.Inject;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

/**
 * Sets the form object to the request.
 *
 * @author kawasima
 */
@Middleware(name = "form", dependencies = {"params", "routing"})
public class FormMiddleware extends AbstractWebMiddleware {
    @Inject
    protected BeansConverter beans;

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain next) {
        Method method = ((Routable) request).getControllerMethod();
        request = MixinUtils.mixin(request, BodyDeserializable.class);
        for (Parameter parameter : method.getParameters()) {
            Class<?> type = parameter.getType();
            if (ParameterUtils.isReservedType(type)) continue;

            BodyDeserializable bodyDeserializable = BodyDeserializable.class.cast(request);
            Object body = bodyDeserializable.getDeserializedBody();
            try {
                if (body == null) {
                    bodyDeserializable.setDeserializedBody(beans.createFrom(request.getParams(), type));
                } else {
                    beans.copy(request.getParams(), body, BeansConverter.CopyOption.REPLACE_NON_NULL);
                    bodyDeserializable.setDeserializedBody(body);
                }
            } catch (ClassCastException e) {
                if (!Serializable.class.isAssignableFrom(type)) {
                    throw new MisconfigurationException("kotowari.FORM_IS_NOT_SERIALIZABLE", type);
                } else {
                    throw new UnreachableException(e);
                }
            }
        }

        return castToHttpResponse(next.next(request));
    }
}
