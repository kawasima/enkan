package enkan.component.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import enkan.component.AbstractBeansConverter;
import enkan.component.ComponentLifecycle;
import enkan.exception.MisconfigurationException;

import java.io.IOException;

/**
 * @author kawasima
 */
public class JacksonBeansConverter extends AbstractBeansConverter<JacksonBeansConverter> {
    private ObjectMapper mapper;
    private ObjectMapper nonNullMapper;

    @Override
    public void copy(Object source, Object destination, CopyOption copyOption) {
        try {
            switch (copyOption) {
                case REPLACE_NON_NULL -> {
                    byte[] buf = nonNullMapper.writeValueAsBytes(source);
                    mapper.readerForUpdating(destination).readValue(buf);
                }
                case REPLACE_ALL -> {
                    byte[] buf = mapper.writeValueAsBytes(source);
                    mapper.readerForUpdating(destination).readValue(buf);
                }
                case PRESERVE_NON_NULL ->
                    throw new MisconfigurationException("jackson.UNSUPPORTED_COPY_OPTION", "PRESERVE_NON_NULL",
                            "Use REPLACE_NON_NULL or REPLACE_ALL instead.");
            }
        } catch (IOException e) {
            throw new MisconfigurationException("jackson.IO_ERROR");
        }
    }

    @Override
    public <S> S createFrom(Object source, Class<S> destinationClass) {
        if (destinationClass == null)
            throw new IllegalArgumentException("destinationClass is null");

        if (Number.class.isAssignableFrom(destinationClass)
                || destinationClass.equals(String.class)) {
            throw new IllegalArgumentException("destinationClass cannot be mapped to JSON object class");
        }
        return mapper.convertValue(source, destinationClass);
    }

    @Override
    protected ComponentLifecycle<JacksonBeansConverter> lifecycle() {
        return new ComponentLifecycle<>() {
            @Override
            public void start(JacksonBeansConverter component) {
                component.mapper = new ObjectMapper();
                component.mapper.registerModule(new JavaTimeModule());
                component.mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
                component.mapper.configure(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS, true);
                component.mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
                component.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                component.nonNullMapper = component.mapper.copy()
                        .setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
            }

            @Override
            public void stop(JacksonBeansConverter component) {
                component.mapper = null;
                component.nonNullMapper = null;
            }
        };
    }

    @Override
    public String toString() {
        return "#JacksonBeansConverter {\n"
                + "  \"dependencies\": " + dependenciesToString()
                + "\n}";
    }
}
