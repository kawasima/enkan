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

    protected <T extends Serializable> T createForm(Class<T> formClass, Map<String, ?> params) {
        try {
            return beans.createFrom(params, formClass);
        } catch (ClassCastException e) {
            if (!Serializable.class.isAssignableFrom(formClass)) {
                throw new MisconfigurationException("kotowari.FORM_IS_NOT_SERIALIZABLE", formClass);
            } else {
                throw new UnreachableException(e);
            }
        }
    }

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain next) {
        Method method = ((Routable) request).getControllerMethod();
        request = MixinUtils.mixin(request, BodyDeserializable.class);
        for (Parameter parameter : method.getParameters()) {
            Class<?> type = parameter.getType();
            if (HttpRequest.class.isAssignableFrom(type)
                    || Session.class.isAssignableFrom(type)
                    || Flash.class.isAssignableFrom(type)
                    || Conversation.class.isAssignableFrom(type)
                    || ConversationState.class.isAssignableFrom(type)
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
