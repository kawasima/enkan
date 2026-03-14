package kotowari.example.graalvm.jaxrs;

import tools.jackson.databind.ObjectMapper;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Objects;

public class JsonBodyReader<T> implements MessageBodyReader<T> {
    private final ObjectMapper mapper;

    public JsonBodyReader(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return Objects.equals(mediaType.getSubtype(), "json");
    }

    @Override
    public T readFrom(Class<T> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                      MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException, WebApplicationException {
        return mapper.readerFor(mapper.getTypeFactory().constructType(genericType))
                .readValue(entityStream);
    }
}
