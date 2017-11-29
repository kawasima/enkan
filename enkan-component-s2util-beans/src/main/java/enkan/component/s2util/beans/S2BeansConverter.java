package enkan.component.s2util.beans;

import enkan.component.BeansConverter;
import enkan.component.ComponentLifecycle;
import enkan.exception.UnreachableException;
import org.seasar.util.beans.factory.BeanDescFactory;
import org.seasar.util.beans.util.BeanUtil;
import org.seasar.util.beans.util.CopyOptions;
import org.seasar.util.beans.util.CopyOptionsUtil;

import java.util.Map;

public class S2BeansConverter extends BeansConverter {
    private static CopyOptions DEFAULT_OPTIONS = new CopyOptions();
    private static CopyOptions EXCLUDE_NULL_OPTIONS = CopyOptionsUtil.excludeNull();

    private CopyOptions createCopyOptions(CopyOption option) {
        switch(option) {
            case REPLACE_ALL:
                return DEFAULT_OPTIONS;
            case REPLACE_NON_NULL:
                return EXCLUDE_NULL_OPTIONS;
            case PRESERVE_NON_NULL:
                throw new UnsupportedOperationException("PRESERVE_NON_NULL");
            default:
                throw new UnreachableException();
        }
    }
    @Override
    public void copy(Object source, Object destination, CopyOption option) {
        CopyOptions copyOptions = createCopyOptions(option);
        if (source instanceof Map) {
            if (destination instanceof Map) {
                BeanUtil.copyMapToMap((Map)source, (Map)destination, copyOptions);
            } else {
                BeanUtil.copyMapToBean((Map) source, destination, copyOptions);
            }
        } else {
            if (destination instanceof Map) {
                BeanUtil.copyBeanToMap(source, (Map) destination, copyOptions);
            } else {
                BeanUtil.copyBeanToBean(source, destination, copyOptions);
            }
        }
    }

    @Override
    public <T> T createFrom(Object source, Class<T> destinationClass) {
        if (source instanceof Map) {
            return (T) BeanUtil.copyMapToNewBean((Map) source, destinationClass);
        } else {
            return BeanUtil.copyBeanToNewBean(source, destinationClass);
        }
    }

    @Override
    protected ComponentLifecycle lifecycle() {
        return new ComponentLifecycle<S2BeansConverter>() {

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
