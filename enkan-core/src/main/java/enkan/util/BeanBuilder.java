package enkan.util;

import enkan.exception.MisconfigurationException;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * An Utility of builder pattern.
 *
 * @author kawasima
 */
public class BeanBuilder<X> {
    private static final ValidatorFactory DEFAULT_VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();
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
