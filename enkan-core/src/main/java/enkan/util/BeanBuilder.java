package enkan.util;

import enkan.exception.MisconfigurationException;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author kawasima
 */
public class BeanBuilder<X> {
    private X x;
    private ValidatorFactory validatorFactory;

    private BeanBuilder(X x, ValidatorFactory validatorFactory) {
        this.x = x;
        this.validatorFactory = validatorFactory;
    }

    public static <X> Function<X,BeanBuilder<X>> builderWithValidation(ValidatorFactory validatorFactory) {
        return bean -> new BeanBuilder<>(bean, validatorFactory);
    }

    public static <X> BeanBuilder<X> builder(X x) {
        return new BeanBuilder<>(x, null);
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

                throw MisconfigurationException.create("BUILD_ERROR", x.getClass(),
                        violations.stream().map(ConstraintViolation::getMessage)
                                .collect(Collectors.joining(",")));
            }
        }
        return x;
    }
}
