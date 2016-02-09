package enkan.component;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import enkan.exception.MisconfigurationException;

import java.io.IOException;

import static enkan.util.ReflectionUtils.tryReflection;

/**
 * @author kawasima
 */
public class JacksonBeansConverter extends BeansConverter {
    private ObjectMapper mapper;

    @Override
    public void copy(Object source, Object destination) {
        try {
            byte[] buf = mapper.writeValueAsBytes(source);
            mapper.readerForUpdating(destination).readValue(buf);
        } catch (IOException e) {
            throw MisconfigurationException.create("JACKSON_ERROR");
        }
    }

    @Override
    public <T> T createFrom(Object source, Class<T> destinationClass) {
        return mapper.convertValue(source, destinationClass);
    }

    @Override
    protected ComponentLifecycle lifecycle() {
        return new ComponentLifecycle<JacksonBeansConverter>() {
            @Override
            public void start(JacksonBeansConverter component) {
                component.mapper = new ObjectMapper();
                component.mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
                component.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            }

            @Override
            public void stop(JacksonBeansConverter component) {
                component.mapper = null;
            }
        };
    }
}
