package enkan.component;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.junit.Test;

import java.io.IOException;

/**
 * @author kawasima
 */
public class JacksonBeansTest {

    @Test
    public void test() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addHandler(new DeserializationProblemHandler() {
            @Override
            public boolean handleUnknownProperty(DeserializationContext ctxt, JsonParser jp, JsonDeserializer<?> deserializer, Object beanOrClass, String propertyName) throws IOException, JsonProcessingException {
                return true;
            }
        });
        TestBean bean = new TestBean("ABC", "12", "Tokyo");
        Person person = mapper.convertValue(bean, Person.class);
        person.getName();
    }

    @Data
    @RequiredArgsConstructor
    public static class TestBean {
        @NonNull
        String name;
        @NonNull
        String age;
        @NonNull
        String address;
    }

    @Data
    public static class Person {
        String name;
        int age;
    }
}
