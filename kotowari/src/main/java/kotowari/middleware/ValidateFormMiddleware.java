package kotowari.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.middleware.AbstractWebMiddleware;
import kotowari.data.FormAvailable;
import kotowari.data.TemplatedHttpResponse;
import org.eclipse.collections.api.multimap.MutableMultimap;
import org.eclipse.collections.impl.factory.Multimaps;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

/**
 * @author kawasima
 */
@Middleware(name = "validateForm", dependencies = "form")
public class ValidateFormMiddleware extends AbstractWebMiddleware {
    private Validator validator;

    public ValidateFormMiddleware() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain next) {
        MutableMultimap<String, String> errors = Multimaps.mutable.list.empty();
        if (request instanceof FormAvailable) {
            Object form = ((FormAvailable) request).getForm();
            if (form != null) {
                Set<ConstraintViolation<Object>> violations = validator.validate(form);
                for (ConstraintViolation<Object> violation : violations) {
                    errors.put(violation.getPropertyPath().toString(), violation.getMessage());
                }
            }
        }
        HttpResponse response =  castToHttpResponse(next.next(request));
        if (!errors.isEmpty()) {
            response.setStatus(400);
            if (response instanceof TemplatedHttpResponse) {
                ((TemplatedHttpResponse) response).getContext().put("errors", errors.toMap());
            }
        }
        return response;
    }
}
