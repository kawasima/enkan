package kotowari.middleware;

import enkan.Middleware;
import enkan.MiddlewareChain;
import enkan.collection.Multimap;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.exception.MisconfigurationException;
import enkan.util.ThreadingUtils;
import kotowari.data.BodyDeserializable;
import kotowari.data.Validatable;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Optional;
import java.util.Set;

/**
 * @author kawasima
 */
@enkan.annotation.Middleware(name = "validateBody")
public class ValidateBodyMiddleware<RES> implements Middleware<HttpRequest, RES, HttpRequest, RES> {
    private static final Validator VALIDATOR = createValidator();

    private static Validator createValidator() {
        try {
            ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            Runtime.getRuntime().addShutdownHook(new Thread(factory::close,
                    "ValidateBodyMiddleware-ValidatorFactory-shutdown"));
            return factory.getValidator();
        } catch (Exception | NoClassDefFoundError e) {
            throw new MisconfigurationException("core.MISSING_IMPLEMENTATION",
                    "ValidateBodyMiddleware requires a Jakarta Validation provider "
                    + "(e.g. hibernate-validator) on the classpath", e);
        }
    }

    protected Validatable getValidatable(HttpRequest request) {
        if (request instanceof BodyDeserializable bd) {
            Object body = bd.getDeserializedBody();
            if (body instanceof Validatable v) {
                return v;
            }
        }
        return null;
    }

    @Override
    public <NNREQ, NNRES> RES handle(HttpRequest request, MiddlewareChain<HttpRequest, RES, NNREQ, NNRES> chain) {

        Optional<Validatable> validatable = ThreadingUtils.some(getValidatable(request), form -> {
            Multimap<String, Object> errors = Multimap.empty();
            Set<ConstraintViolation<Object>> violations = VALIDATOR.validate(form);
            for (ConstraintViolation<Object> violation : violations) {
                errors.add(violation.getPropertyPath().toString(), violation.getMessage());
            }
            form.setErrors(errors);
            return form;
        });

        RES response = chain.next(request);
        if (response instanceof HttpResponse httpResponse
                && validatable.isPresent()
                && validatable.get().hasErrors()) {
            httpResponse.setStatus(400);
        }
        return response;
    }
}
