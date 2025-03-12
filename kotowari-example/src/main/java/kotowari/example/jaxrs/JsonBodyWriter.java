package kotowari.example.jaxrs;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Objects;

public class JsonBodyWriter<T> implements MessageBodyWriter<T> {
    private final ObjectMapper mapper;

    public JsonBodyWriter(ObjectMapper mapper) {
        this.mapper = mapper;
    }
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return Objects.equals(mediaType.getSubtype(), "json");
    }

    @Override
    public void writeTo(T o, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        mapper.writerFor(mapper.constructType(genericType))
                .writeValue(entityStream, o);
    }
}
