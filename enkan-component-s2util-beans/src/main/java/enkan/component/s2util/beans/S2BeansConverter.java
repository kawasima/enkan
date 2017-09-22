package enkan.component.s2util.beans;

import enkan.component.BeansConverter;
import enkan.component.ComponentLifecycle;
import org.seasar.util.beans.factory.BeanDescFactory;
import org.seasar.util.beans.util.BeanUtil;

import java.util.Map;

public class S2BeansConverter extends BeansConverter {
    @Override
    public void copy(Object source, Object destination) {
        if (source instanceof Map) {
            if (destination instanceof Map) {
                BeanUtil.copyMapToMap((Map)source, (Map)destination);
            } else {
                BeanUtil.copyMapToBean((Map) source, destination);
            }
        } else {
            if (destination instanceof Map) {
                BeanUtil.copyBeanToMap(source, (Map) destination);
            } else {
                BeanUtil.copyBeanToBean(source, destination);
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
