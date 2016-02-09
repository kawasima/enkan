package kotowari.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.collection.Multimap;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.middleware.AbstractWebMiddleware;
import enkan.util.ThreadingUtils;
import kotowari.data.FormAvailable;
import kotowari.data.Validatable;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Optional;
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

    protected Validatable getValidatableForm(HttpRequest request) {
        if (request instanceof FormAvailable) {
            Object form = ((FormAvailable) request).getForm();
            if (form != null && form instanceof Validatable) {
                return (Validatable) form;
            }
        }
        return null;
    }

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain next) {

        Optional<Validatable> validatableForm = ThreadingUtils.some(getValidatableForm(request), form -> {
            Multimap<String, String> errors = Multimap.empty();
            Set<ConstraintViolation<Object>> violations = validator.validate(form);
            for (ConstraintViolation<Object> violation : violations) {
                errors.add(violation.getPropertyPath().toString(), violation.getMessage());
            }
            form.setErrors(errors);
            return form;
        });

        HttpResponse response =  castToHttpResponse(next.next(request));
        if (validatableForm.isPresent() && validatableForm.get().hasErrors()) {
            response.setStatus(400);
        }
        return response;
    }
}
