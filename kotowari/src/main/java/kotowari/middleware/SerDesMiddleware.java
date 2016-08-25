package kotowari.middleware;

import enkan.Middleware;
import enkan.MiddlewareChain;
import enkan.collection.Headers;
import enkan.data.*;
import enkan.security.UserPrincipal;
import enkan.util.CodecUtils;
import enkan.util.HttpRequestUtils;
import enkan.util.MixinUtils;
import kotowari.data.BodyDeserializable;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.*;

import static enkan.util.BeanBuilder.builder;

/**
 * Serialize a java object to response body and deserialize  a response body
 * to a java object.
 *
 * @author kawasima
 */
@enkan.annotation.Middleware(name = "serDes", dependencies = {"contentNegotiation"})
public class SerDesMiddleware implements Middleware<HttpRequest, HttpResponse> {
    private final List<MessageBodyReader> bodyReaders = new ArrayList<>();
    private final List<MessageBodyWriter> bodyWriters = new ArrayList<>();

    public SerDesMiddleware() {
        loadReaderAndWriter();
    }

    private void loadReaderAndWriter() {
        ClassLoader cl = Optional.ofNullable(Thread.currentThread().getContextClassLoader())
                .orElse(getClass().getClassLoader());
        for (MessageBodyReader reader : ServiceLoader.load(MessageBodyReader.class, cl)) {
            bodyReaders.add(reader);
        }
        for (MessageBodyWriter writer : ServiceLoader.load(MessageBodyWriter.class, cl)) {
            bodyWriters.add(writer);
        }
    }

    protected <T> T deserialize(HttpRequest request, Class<T> clazz, Type type, MediaType mediaType) {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        return bodyReaders.stream()
                .filter(reader -> reader.isReadable(clazz, type, null, mediaType))
                .map(reader -> {
                    try {
                        return (T) reader.readFrom(clazz, type, null, mediaType, headers, request.getBody());
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    protected InputStream serialize(Object obj, MediaType mediaType) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        return bodyWriters.stream()
                .filter(writer -> writer.isWriteable(obj.getClass(), obj.getClass(), null, mediaType))
                .map(writer -> {
                    try {
                        writer.writeTo(obj, obj.getClass(), obj.getClass(), null, mediaType, headers, baos);
                        return baos.toByteArray();
                    } catch (IOException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .map(ByteArrayInputStream::new)
                .findFirst()
                .orElse(null);
    }

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain chain) {
        Method method = ((Routable) request).getControllerMethod();
        request = MixinUtils.mixin(request, BodyDeserializable.class);
        String contentType = HttpRequestUtils.contentType(request);

        if (contentType != null && !HttpRequestUtils.isUrlEncodedForm(request)) {
            String[] mediaTypeTokens = contentType.split("/", 2);
            MediaType mediaType = new MediaType(mediaTypeTokens[0], mediaTypeTokens[1]);
            for (Parameter parameter : method.getParameters()) {

                Class type = parameter.getType();
                Type genericType = parameter.getParameterizedType();

                if (HttpRequest.class.isAssignableFrom(type)
                        || Session.class.isAssignableFrom(type)
                        || UserPrincipal.class.isAssignableFrom(type)
                        || Map.class.isAssignableFrom(type)) {
                    continue;
                }

                ((BodyDeserializable) request).setDeserializedBody(
                        deserialize(request, type, genericType, mediaType));
            }
        }

        Object response = chain.next(request);
        if (HttpResponse.class.isInstance(response)) {
            return (HttpResponse) response;
        } else {
            MediaType responseType = ContentNegotiable.class.cast(request).getMediaType();
            InputStream in = serialize(response, responseType);
            if (in != null) {
                return builder(HttpResponse.of(in))
                        .set(HttpResponse::setHeaders,
                                Headers.of("Content-Type", CodecUtils.printMediaType(responseType)))
                        .build();
            } else {
                return builder(HttpResponse.of("Not acceptable"))
                        .set(HttpResponse::setStatus, 406)
                        .build();
            }
        }
    }

    public void setBodyReaders(MessageBodyReader... readers) {
        bodyReaders.addAll(Arrays.asList(readers));
    }

    public void setBodyWriters(MessageBodyWriter... writers) {
        bodyWriters.addAll(Arrays.asList(writers));
    }

}
