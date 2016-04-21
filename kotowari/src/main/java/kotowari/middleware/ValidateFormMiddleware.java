package kotowari.middleware;

import enkan.Middleware;
import enkan.MiddlewareChain;
import enkan.collection.Multimap;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.util.ThreadingUtils;
import kotowari.data.BodyDeserializable;
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
@enkan.annotation.Middleware(name = "validateForm", dependencies = "form")
public class ValidateFormMiddleware<RES> implements Middleware<HttpRequest, RES> {
    private Validator validator;

    public ValidateFormMiddleware() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    protected Validatable getValidatableForm(HttpRequest request) {
        if (request instanceof BodyDeserializable) {
            Object form = ((BodyDeserializable) request).getDeserializedBody();
            if (form != null && form instanceof Validatable) {
                return (Validatable) form;
            }
        }
        return null;
    }

    @Override
    public RES handle(HttpRequest request, MiddlewareChain next) {

        Optional<Validatable> validatableForm = ThreadingUtils.some(getValidatableForm(request), form -> {
            Multimap<String, String> errors = Multimap.empty();
            Set<ConstraintViolation<Object>> violations = validator.validate(form);
            for (ConstraintViolation<Object> violation : violations) {
                errors.add(violation.getPropertyPath().toString(), violation.getMessage());
            }
            form.setErrors(errors);
            return form;
        });

        RES response = (RES) next.next(request);
        if (HttpResponse.class.isInstance(response)
                && validatableForm.isPresent()
                && validatableForm.get().hasErrors()) {
            HttpResponse.class.cast(response).setStatus(400);
        }
        return response;
    }
}
