package enkan.component.s2util.beans;

import enkan.component.AbstractBeansConverter;
import enkan.component.ComponentLifecycle;
import org.seasar.util.beans.factory.BeanDescFactory;
import org.seasar.util.beans.util.BeanUtil;
import org.seasar.util.beans.util.CopyOptions;
import org.seasar.util.beans.util.CopyOptionsUtil;

import java.util.Map;

public class S2BeansConverter extends AbstractBeansConverter<S2BeansConverter> {
    private static final CopyOptions DEFAULT_OPTIONS = new CopyOptions();
    private static final CopyOptions EXCLUDE_NULL_OPTIONS = CopyOptionsUtil.excludeNull();

    private CopyOptions createCopyOptions(CopyOption option) {
        return switch (option) {
            case REPLACE_ALL -> DEFAULT_OPTIONS;
            case REPLACE_NON_NULL -> EXCLUDE_NULL_OPTIONS;
            case PRESERVE_NON_NULL -> throw new UnsupportedOperationException("PRESERVE_NON_NULL");
        };
    }
    @SuppressWarnings("unchecked")
    @Override
    public void copy(Object source, Object destination, CopyOption option) {
        CopyOptions copyOptions = createCopyOptions(option);
        if (source instanceof Map) {
            if (destination instanceof Map) {
                BeanUtil.copyMapToMap((Map<String, ?>)source, (Map<String, Object>)destination, copyOptions);
            } else {
                BeanUtil.copyMapToBean((Map<String, ?>) source, destination, copyOptions);
            }
        } else {
            if (destination instanceof Map) {
                BeanUtil.copyBeanToMap(source, (Map<String, Object>) destination, copyOptions);
            } else {
                BeanUtil.copyBeanToBean(source, destination, copyOptions);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T createFrom(Object source, Class<T> destinationClass) {
        if (source instanceof Map) {
            return (T) BeanUtil.copyMapToNewBean((Map<String, ?>) source, destinationClass);
        } else {
            return BeanUtil.copyBeanToNewBean(source, destinationClass);
        }
    }

    @Override
    protected ComponentLifecycle<S2BeansConverter> lifecycle() {
        return new ComponentLifecycle<>() {

            @Override
            public void start(S2BeansConverter component) {
                BeanDescFactory.initialize();
            }

            @Override
            public void stop(S2BeansConverter component) {
                BeanDescFactory.clear();
            }
        };
    }
}
