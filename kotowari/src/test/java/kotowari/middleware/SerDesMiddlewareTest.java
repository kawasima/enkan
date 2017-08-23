package kotowari.middleware;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import enkan.Endpoint;
import enkan.MiddlewareChain;
import enkan.chain.DefaultMiddlewareChain;
import enkan.collection.Headers;
import enkan.data.*;
import enkan.predicate.AnyPredicate;
import enkan.util.MixinUtils;
import kotowari.data.BodyDeserializable;
import lombok.Data;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.io.*;
import java.lang.reflect.Method;
import java.util.List;

import static enkan.util.BeanBuilder.builder;
import static enkan.util.ReflectionUtils.tryReflection;

/**
 * @author kawasima
 */
public class SerDesMiddlewareTest {
    final JacksonJsonProvider jsonProvider = new JacksonJsonProvider();

    @Test
    public void test() {
        SerDesMiddleware middleware = builder(new SerDesMiddleware())
                .set(SerDesMiddleware::setBodyReaders, jsonProvider)
                .build();

        String body = "{\"a\":1, \"b\":\"ccb\"}";
        HttpRequest request = builder(new DefaultHttpRequest())
                .set(HttpRequest::setHeaders, Headers.of("Content-Type", "application/json"))
                .set(HttpRequest::setBody, new ByteArrayInputStream(body.getBytes()))
                .build();

        TestDto testDto = middleware.deserialize(request, TestDto.class, TestDto.class, new MediaType("application", "json"));
        System.out.println(testDto);
    }

    @Test
    public void deserializeList() {
        SerDesMiddleware middleware = builder(new SerDesMiddleware())
                .set(SerDesMiddleware::setBodyReaders, jsonProvider)
                .build();

        String body = "[{\"a\":1, \"b\":\"ccb\"}, {\"a\":2, \"b\":\"ooo\"}]";
        HttpRequest request = builder(new DefaultHttpRequest())
                .set(HttpRequest::setHeaders, Headers.of("Content-Type", "application/json"))
                .set(HttpRequest::setBody, new ByteArrayInputStream(body.getBytes()))
                .build();

        Object testDto = middleware.deserialize(request, TestDto.class, List.class, new MediaType("application", "json"));
        System.out.println(testDto);
    }

    @Test
    public void controller() throws IOException {
        SerDesMiddleware middleware = builder(new SerDesMiddleware())
                .set(SerDesMiddleware::setBodyReaders, jsonProvider)
                .build();

        String body = "[{\"a\":1, \"b\":\"ccb\"}, {\"a\":2, \"b\":\"ooo\"}]";
        HttpRequest request = MixinUtils.mixin(builder(new DefaultHttpRequest())
                .set(HttpRequest::setHeaders, Headers.of("Content-Type", "application/json"))
                .set(HttpRequest::setBody, new ByteArrayInputStream(body.getBytes()))
                .build(), Routable.class, ContentNegotiable.class);
        ContentNegotiable.class.cast(request).setMediaType(new MediaType("application", "json"));
        TestController controller = new TestController();
        MiddlewareChain<HttpRequest, HttpResponse> chain = new DefaultMiddlewareChain(new AnyPredicate(), null,
                (Endpoint<HttpRequest, ?>) req ->
                        tryReflection(() -> {
                            List<TestDto> obj = BodyDeserializable.class.cast(req).getDeserializedBody();
                            return Routable.class.cast(req).getControllerMethod().invoke(controller, obj);
                        }));

        HttpResponse<InputStream> resp = tryReflection(() -> {
            Method fooMethod = TestController.class.getMethod("foo", List.class);
            Routable.class.cast(request).setControllerMethod(fooMethod);
            return middleware.handle(request, chain);
        });

        try(BufferedReader rdr = new BufferedReader(new InputStreamReader(resp.getBody()))) {
            rdr.lines().forEach(System.out::println);
        }
    }

    @Test
    public void nullSerialize() {
        SerDesMiddleware middleware = builder(new SerDesMiddleware())
                .set(SerDesMiddleware::setBodyReaders, jsonProvider)
                .build();
        middleware.serialize(null, new MediaType("application", "json"));
    }

    @Data
    static class TestDto {
        private int a;
        private String b;
    }

    static class TestController {
        public TestDto foo(List<TestDto> testDtoList) {
            return testDtoList.get(0);
        }
    }
}
