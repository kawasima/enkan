package kotowari.middleware;

import enkan.Middleware;
import enkan.MiddlewareChain;
import enkan.collection.Headers;
import enkan.component.BeansConverter;
import enkan.component.SystemComponent;
import enkan.data.ContentNegotiable;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.data.Routable;
import enkan.system.inject.ComponentInjector;
import enkan.util.CodecUtils;
import enkan.util.HttpRequestUtils;
import enkan.util.MixinUtils;
import kotowari.data.BodyDeserializable;
import kotowari.util.ParameterUtils;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.*;

import static enkan.util.BeanBuilder.*;

/**
 * Serialize a java object to response body and deserialize  a response body
 * to a java object.
 *
 * @author kawasima
 */
@enkan.annotation.Middleware(name = "serDes", dependencies = {"contentNegotiation", "routing"})
public class SerDesMiddleware<NRES> implements Middleware<HttpRequest, HttpResponse, HttpRequest, NRES> {
    @Inject
    protected BeansConverter beans;

    private final List<MessageBodyReader<?>> bodyReaders = new ArrayList<>();
    private final List<MessageBodyWriter<?>> bodyWriters = new ArrayList<>();

    @PostConstruct
    private void loadReaderAndWriter() {
        Map<String, SystemComponent> components = new HashMap<>();
        components.put("beans", beans);
        ComponentInjector injector = new ComponentInjector(components);
        ClassLoader cl = Optional.ofNullable(Thread.currentThread().getContextClassLoader())
                .orElse(getClass().getClassLoader());
        for (MessageBodyReader reader : ServiceLoader.load(MessageBodyReader.class, cl)) {
            injector.inject(reader);
            bodyReaders.add(reader);
        }
        for (MessageBodyWriter writer : ServiceLoader.load(MessageBodyWriter.class, cl)) {
            injector.inject(writer);
            bodyWriters.add(writer);
        }
    }

    @SuppressWarnings("unchecked")
    protected <T> T deserialize(HttpRequest request, Class<T> type, Type genericType, MediaType mediaType) {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        return bodyReaders.stream()
                .filter(reader -> reader.isReadable(type, genericType, null, mediaType))
                .map(reader -> {
                    try {
                        return (T) MessageBodyReader.class.cast(reader)
                                .readFrom(type, genericType, null, mediaType, headers, request.getBody());
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                })
                .findAny()
                .orElse(null);
    }

    @SuppressWarnings("unchecked")
    protected InputStream serialize(Object obj, MediaType mediaType) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();

        if (obj == null) {
            return new ByteArrayInputStream(new byte[0]);
        }
        return bodyWriters.stream()
                .filter(writer -> writer.isWriteable(obj.getClass(), obj.getClass(), null, mediaType))
                .map(writer -> {
                    try {
                        MessageBodyWriter.class.cast(writer).writeTo(obj, obj.getClass(), obj.getClass(), null, mediaType, headers, baos);
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

    private void deserializeBody(Method method, HttpRequest request) {
        String contentType = HttpRequestUtils.contentType(request);
        if (contentType == null) return;

        String[] mediaTypeTokens = contentType.split("/", 2);
        if (mediaTypeTokens.length == 2) {
            MediaType mediaType = new MediaType(mediaTypeTokens[0], mediaTypeTokens[1]);
            for (Parameter parameter : method.getParameters()) {

                Class<?> type = parameter.getType();
                Type genericType = parameter.getParameterizedType();

                if (ParameterUtils.isReservedType(type)) continue;

                BodyDeserializable bodyDeserializable = BodyDeserializable.class.cast(request);
                Object body = deserialize(request, type, genericType, mediaType);
                bodyDeserializable.setDeserializedBody(body);
            }
        }
    }
    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain<HttpRequest, NRES, ?, ?> chain) {
        Method method = ((Routable) request).getControllerMethod();
        request = MixinUtils.mixin(request, BodyDeserializable.class);
        if (HttpRequestUtils.isUrlEncodedForm(request)) {
            BodyDeserializable bodyDeserializable = BodyDeserializable.class.cast(request);
            if (bodyDeserializable.getDeserializedBody() == null) {
                for (Parameter parameter : method.getParameters()) {
                    Class<?> type = parameter.getType();
                    if (ParameterUtils.isReservedType(type)) continue;
                    bodyDeserializable.setDeserializedBody(beans.createFrom(
                            request.getParams(), type
                    ));
                }
            }
        } else {
            deserializeBody(method, request);
        }

        NRES response = chain.next(request);
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

    public void setBodyReaders(MessageBodyReader<?>... readers) {
        bodyReaders.addAll(Arrays.asList(readers));
    }

    public void setBodyWriters(MessageBodyWriter<?>... writers) {
        bodyWriters.addAll(Arrays.asList(writers));
    }

}
