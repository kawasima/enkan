package kotowari.middleware;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import enkan.Endpoint;
import enkan.MiddlewareChain;
import enkan.chain.DefaultMiddlewareChain;
import enkan.collection.Headers;
import enkan.data.*;
import enkan.predicate.AnyPredicate;
import enkan.util.MixinUtils;
import kotowari.data.BodyDeserializable;
import kotowari.test.dto.TestDto;
import kotowari.util.ParameterUtils;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static enkan.util.BeanBuilder.*;
import static enkan.util.ReflectionUtils.*;
import static org.assertj.core.api.Assertions.*;

/**
 * @author kawasima
 */
public class SerDesMiddlewareTest {
    final JacksonJsonProvider jsonProvider = new JacksonJsonProvider();

    @Test
    public void test() {
        SerDesMiddleware<Object> middleware = builder(new SerDesMiddleware<>())
                .set(SerDesMiddleware::setBodyReaders, jsonProvider)
                .set(SerDesMiddleware::setBodyWriters, jsonProvider)
                .build();

        String body = "{\"a\":1, \"b\":\"ccb\"}";
        HttpRequest request = builder(new DefaultHttpRequest())
                .set(HttpRequest::setHeaders, Headers.of("Content-Type", "application/json"))
                .set(HttpRequest::setBody, new ByteArrayInputStream(body.getBytes()))
                .build();

        TestDto testDto = middleware.deserialize(request, TestDto.class, TestDto.class, new MediaType("application", "json"));
        assertThat(testDto.getA()).isEqualTo(1);
        assertThat(testDto.getB()).isEqualTo("ccb");
    }

    @Test
    public void deserializeList() {
        SerDesMiddleware<Object> middleware = builder(new SerDesMiddleware<>())
                .set(SerDesMiddleware::setBodyReaders, jsonProvider)
                .set(SerDesMiddleware::setBodyWriters, jsonProvider)
                .build();

        String body = "[{\"a\":1, \"b\":\"ccb\"}, {\"a\":2, \"b\":\"ooo\"}]";
        HttpRequest request = builder(new DefaultHttpRequest())
                .set(HttpRequest::setHeaders, Headers.of("Content-Type", "application/json"))
                .set(HttpRequest::setBody, new ByteArrayInputStream(body.getBytes()))
                .build();

        Object testDto = middleware.deserialize(request, List.class, new TypeReference<List<TestDto>>(){}.getType(), new MediaType("application", "json"));
        assertThat(testDto).isInstanceOf(List.class);
        assertThat(testDto).asList().size().isEqualTo(2);
        assertThat(testDto).asList().contains(builder(new TestDto())
                .set(TestDto::setA, 1)
                .set(TestDto::setB, "ccb")
                .build());
    }

    @Test
    public void controller() throws IOException {
        SerDesMiddleware<Object> middleware = builder(new SerDesMiddleware<>())
                .set(SerDesMiddleware::setBodyReaders, jsonProvider)
                .set(SerDesMiddleware::setBodyWriters, jsonProvider)
                .set(SerDesMiddleware::setParameterInjectors, ParameterUtils.getDefaultParameterInjectors())
                .build();

        String body = "[{\"a\":1, \"b\":\"ccb\"}, {\"a\":2, \"b\":\"ooo\"}]";
        HttpRequest request = MixinUtils.mixin(builder(new DefaultHttpRequest())
                .set(HttpRequest::setHeaders, Headers.of("Content-Type", "application/json"))
                .set(HttpRequest::setBody, new ByteArrayInputStream(body.getBytes()))
                .build(), Routable.class, ContentNegotiable.class);
        ContentNegotiable.class.cast(request).setMediaType(new MediaType("application", "json"));
        TestController controller = new TestController();
        MiddlewareChain<HttpRequest, Object, ?, ?> chain = new DefaultMiddlewareChain<>(new AnyPredicate<>(), null,
                (Endpoint<HttpRequest, Object>) req ->
                        tryReflection(() -> {
                            List<TestDto> obj = BodyDeserializable.class.cast(req).getDeserializedBody();
                            return Routable.class.cast(req).getControllerMethod().invoke(controller, obj);
                        }));

        HttpResponse resp = tryReflection(() -> {
            Method fooMethod = TestController.class.getMethod("foo", List.class);
            Routable.class.cast(request).setControllerMethod(fooMethod);
            return middleware.handle(request, chain);
        });

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> res = mapper.readValue(resp.getBodyAsStream(), new TypeReference<Map<String, Object>>(){});
        assertThat(res).containsEntry("a", 1);
        assertThat(res).containsEntry("b", "ccb");
    }

    @Test
    public void nullSerialize() {
        SerDesMiddleware<Object> middleware = builder(new SerDesMiddleware<>())
                .set(SerDesMiddleware::setBodyReaders, jsonProvider)
                .build();
        middleware.serialize(null, new MediaType("application", "json"));
    }

    @Test
    public void raiseException() {
        assertThatThrownBy(() -> {
            SerDesMiddleware<Object> middleware = builder(new SerDesMiddleware<>())
                    .set(SerDesMiddleware::setBodyReaders, jsonProvider)
                    .set(SerDesMiddleware::setBodyWriters, jsonProvider)
                    .build();

            String body = "{\"a\":1, \"b\":\"ccb\", \"c\": \"This is an unknown property.\"}";
            HttpRequest request = builder(new DefaultHttpRequest())
                    .set(HttpRequest::setHeaders, Headers.of("Content-Type", "application/json"))
                    .set(HttpRequest::setBody, new ByteArrayInputStream(body.getBytes()))
                    .build();

            middleware.deserialize(request, TestDto.class, TestDto.class, new MediaType("application", "json"));
        }).hasCauseInstanceOf(UnrecognizedPropertyException.class);
    }

    static class TestController {
        public TestDto foo(List<TestDto> testDtoList) {
            return testDtoList.get(0);
        }
    }
}
