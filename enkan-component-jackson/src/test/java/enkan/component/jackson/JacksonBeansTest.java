package enkan.component.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

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

    @Test
    public void mapFromHashMap() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addHandler(new DeserializationProblemHandler() {
            @Override
            public boolean handleUnknownProperty(DeserializationContext ctxt, JsonParser jp, JsonDeserializer<?> deserializer, Object beanOrClass, String propertyName) throws IOException, JsonProcessingException {
                return true;
            }
        });
        Map<String, Object> m = new HashMap<>();
        m.put("name", "Jackson");
        m.put("age", 10);
        Person person = mapper.convertValue(m, Person.class);
        assertEquals(person.getName(), "Jackson");
    }

    @Test
    public void mapFromHashMapMismatchingTypes() {
        SimpleModule module = new SimpleModule();
        module.setDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
                JsonDeserializer<?> jsonDeserializer = super.modifyDeserializer(config, beanDesc, deserializer);
                if (jsonDeserializer instanceof BeanDeserializerBase) {
                    return new BeanDeserializer((BeanDeserializerBase) jsonDeserializer) {
                        @Override
                        public void wrapAndThrow(Throwable t, Object bean, String fieldName, DeserializationContext ctxt)
                                throws IOException {
                            SettableBeanProperty prop = _beanProperties.find(fieldName);
                        }
                    };
                } else {
                    return jsonDeserializer;
                }
            }
        });
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS, true);
        mapper.registerModule(module);
        mapper.addHandler(new DeserializationProblemHandler() {
            @Override
            public boolean handleUnknownProperty(DeserializationContext ctxt, JsonParser jp, JsonDeserializer<?> deserializer, Object beanOrClass, String propertyName) throws IOException, JsonProcessingException {
                return true;
            }
        });
        Map<String, Object> m = new HashMap<>();
        m.put("name", "Jackson");
        m.put("telNumbers", new ArrayList(){{add("A"); add("B"); add("C");}});
        m.put("age", new int[]{ 10, 20 });
        Person person = mapper.convertValue(m, Person.class);
        assertEquals(person.getName(), "Jackson");
        Assert.assertNotNull(person.getTelNumbers());
        assertEquals(3, person.getTelNumbers().size());
        // FIXME assertEquals(10, person.getAge());
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
        List<String> telNumbers;
    }
}
