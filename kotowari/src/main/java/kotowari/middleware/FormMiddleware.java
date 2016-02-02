package kotowari.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.middleware.AbstractWebMiddleware;
import enkan.util.MixinUtils;
import kotowari.data.FormAvailable;
import enkan.data.Routable;
import org.apache.commons.beanutils.BeanUtils;
import org.eclipse.collections.api.multimap.Multimap;

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
    protected Serializable createForm(Class<?> formClass, Multimap<String, String> params) {
        return tryReflection(() -> {
            Serializable form = (Serializable) formClass.newInstance();
            BeanUtils.populate(form, params.toMap());
            return form;
        });
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
            FormAvailable.class.cast(request).setForm(createForm(type, request.getParams()));
        }

        return castToHttpResponse(next.next(request));
    }
}
