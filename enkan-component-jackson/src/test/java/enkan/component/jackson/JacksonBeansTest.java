package enkan.component.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import enkan.component.BeansConverter;
import enkan.system.EnkanSystem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * @author kawasima
 */
public class JacksonBeansTest {
    private EnkanSystem system;

    @BeforeEach
    public void setUp() {
        system = EnkanSystem.of("beans", new JacksonBeansConverter());
        system.start();
    }

    @AfterEach
    public void tearDown() {
        system.stop();
    }
    @Test
    public void test() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addHandler(new DeserializationProblemHandler() {
            @Override
            public boolean handleUnknownProperty(DeserializationContext ctxt, JsonParser jp, JsonDeserializer<?> deserializer, Object beanOrClass, String propertyName) throws IOException {
                return true;
            }
        });
        TestBean bean = new TestBean("ABC", "12", "Tokyo");
        Person person = mapper.convertValue(bean, Person.class);
        assertThat(person.getName()).isEqualTo("ABC");
    }

    @Test
    public void mapFromHashMap() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addHandler(new DeserializationProblemHandler() {
            @Override
            public boolean handleUnknownProperty(DeserializationContext ctxt, JsonParser jp, JsonDeserializer<?> deserializer, Object beanOrClass, String propertyName) throws IOException {
                return true;
            }
        });
        Map<String, Object> m = new HashMap<>();
        m.put("name", "Jackson");
        m.put("age", 10);
        Person person = mapper.convertValue(m, Person.class);
        assertThat(person.getName()).isEqualTo("Jackson");
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
            public boolean handleUnknownProperty(DeserializationContext ctxt, JsonParser jp, JsonDeserializer<?> deserializer, Object beanOrClass, String propertyName) throws IOException {
                return true;
            }
        });
        Map<String, Object> m = new HashMap<>();
        m.put("name", "Jackson");
        m.put("telNumbers", new ArrayList(){{add("A"); add("B"); add("C");}});
        m.put("age", new int[]{ 10, 20 });
        Person person = mapper.convertValue(m, Person.class);
        assertThat(person.getName()).isEqualTo("Jackson");
        assertThat(person.getTelNumbers()).isNotNull();
        assertThat(person.getTelNumbers().size()).isEqualTo(3);
    }

    @Test
    public void testNull() throws Exception {
        BeansConverter beansConverter = system.getComponent("beans");

        Object bean1 = new TestBean(null, "10", "TOKYO");
        Person dest = new Person();
        dest.name = "GGG";

        beansConverter.copy(bean1, dest, BeansConverter.CopyOption.REPLACE_NON_NULL);
        assertThat(dest.getName()).isEqualTo("GGG");
    }

    @Test
    public void idempotence() {
        BeansConverter beansConverter = system.getComponent("beans");

        Object bean1 = new TestBean(null, "10", "TOKYO");
        Person dest = new Person();
        dest.name = "GGG";
        beansConverter.copy(bean1, dest, BeansConverter.CopyOption.REPLACE_NON_NULL);
        assertThat(dest.getName()).isEqualTo("GGG");

        beansConverter.copy(bean1, dest);
        assertThat(dest.getName()).isNull();
    }

    @Data
    @AllArgsConstructor
    public static class TestBean {
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
