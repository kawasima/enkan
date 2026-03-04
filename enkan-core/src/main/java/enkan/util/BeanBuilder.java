package enkan.util;

import enkan.exception.MisconfigurationException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A Utility of builder pattern.
 *
 * @author kawasima
 */
public class BeanBuilder<X> {
    private static final ValidatorFactory DEFAULT_VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();

    static {
        // ValidatorFactory is Closeable; close it when the JVM shuts down to
        // release resources (connection pools, etc.) held by the provider.
        Runtime.getRuntime().addShutdownHook(new Thread(DEFAULT_VALIDATOR_FACTORY::close,
                "BeanBuilder-ValidatorFactory-shutdown"));
    }

    private final X x;
    private final ValidatorFactory validatorFactory;

    private BeanBuilder(X x, ValidatorFactory validatorFactory) {
        this.x = x;
        this.validatorFactory = validatorFactory;
    }

    public static <X> Function<X,BeanBuilder<X>> builderWithValidation(ValidatorFactory validatorFactory) {
        return bean -> new BeanBuilder<>(bean, validatorFactory);
    }

    public static <Y> BeanBuilder<Y> builder(Y x) {
        return new BeanBuilder<>(x, DEFAULT_VALIDATOR_FACTORY);
    }

    public <V> BeanBuilder<X> set(BiConsumer<X, V> caller, V v) {
        caller.accept(x, v);
        return this;
    }

    public X build() {
        if (validatorFactory != null) {
            Validator validator = validatorFactory.getValidator();
            Set<ConstraintViolation<X>> violations = validator.validate(x);
            if (!violations.isEmpty()) {
                throw new MisconfigurationException("core.BUILD_ERROR", x.getClass().getName(),
                        violations.stream().map(ConstraintViolation::getMessage)
                                .collect(Collectors.joining(",")));
            }
        }
        return x;
    }
}
