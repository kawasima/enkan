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
@enkan.annotation.Middleware(name = "validateBody")
public class ValidateBodyMiddleware<RES> implements Middleware<HttpRequest, RES> {
    private Validator validator;

    public ValidateBodyMiddleware() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    protected Validatable getValidatable(HttpRequest request) {
        if (request instanceof BodyDeserializable) {
            Object body = ((BodyDeserializable) request).getDeserializedBody();
            if (body != null && body instanceof Validatable) {
                return (Validatable) body;
            }
        }
        return null;
    }

    @Override
    public RES handle(HttpRequest request, MiddlewareChain next) {

        Optional<Validatable> validatable = ThreadingUtils.some(getValidatable(request), form -> {
            Multimap<String, Object> errors = Multimap.empty();
            Set<ConstraintViolation<Object>> violations = validator.validate(form);
            for (ConstraintViolation<Object> violation : violations) {
                errors.add(violation.getPropertyPath().toString(), violation.getMessage());
            }
            form.setErrors(errors);
            return form;
        });

        RES response = (RES) next.next(request);
        if (HttpResponse.class.isInstance(response)
                && validatable.isPresent()
                && validatable.get().hasErrors()) {
            HttpResponse.class.cast(response).setStatus(400);
        }
        return response;
    }
}
