package kotowari.example.jaxrs;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Objects;

public class JsonBodyWriter implements MessageBodyWriter {
    private final ObjectMapper mapper;

    public JsonBodyWriter(ObjectMapper mapper) {
        this.mapper = mapper;
    }
    @Override
    public boolean isWriteable(Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return Objects.equals(mediaType.getSubtype(), "json");
    }

    @Override
    public void writeTo(Object o, Class type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        mapper.writerFor(mapper.constructType(genericType))
                .writeValue(entityStream, o);
    }
}
