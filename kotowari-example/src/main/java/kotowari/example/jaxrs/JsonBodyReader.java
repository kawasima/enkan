package kotowari.example.jaxrs;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Objects;

public class JsonBodyReader implements MessageBodyReader {
    private final ObjectMapper mapper;

    public JsonBodyReader(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public boolean isReadable(Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return Objects.equals(mediaType.getSubtype(), "json");
    }

    @Override
    public Object readFrom(Class type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        return mapper.readerFor(mapper.constructType(genericType))
                .readValue(entityStream);
    }
}
