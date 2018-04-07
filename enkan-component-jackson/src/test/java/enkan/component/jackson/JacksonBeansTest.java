package enkan.component.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import enkan.component.BeansConverter;
import enkan.system.EnkanSystem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
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
        m.put("telNumbers", new ArrayList<String>(){{add("A"); add("B"); add("C");}});
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

    @Test
    public void createFromIllegalArgument() {
        BeansConverter beansConverter = system.getComponent("beans");
        Person person = new Person();
        person.name = "GGG";
        person.age = 10;

        assertThatThrownBy(() -> beansConverter.createFrom(person, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> beansConverter.createFrom(person, String.class))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> beansConverter.createFrom(person, ArrayList.class))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> beansConverter.createFrom(person, BigDecimal.class))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> beansConverter.createFrom(person, Object[].class))
                .isInstanceOf(IllegalArgumentException.class);
    }

    public static class TestBean implements Serializable {
        String name;
        String age;
        String address;

        @java.beans.ConstructorProperties({"name", "age", "address"})
        public TestBean(String name, String age, String address) {
            this.name = name;
            this.age = age;
            this.address = address;
        }

        public String getName() {
            return this.name;
        }

        public String getAge() {
            return this.age;
        }

        public String getAddress() {
            return this.address;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setAge(String age) {
            this.age = age;
        }

        public void setAddress(String address) {
            this.address = address;
        }


        public String toString() {
            return "JacksonBeansTest.TestBean(name=" + this.getName() + ", age=" + this.getAge() + ", address=" + this.getAddress() + ")";
        }
    }

    public static class Person implements Serializable {
        String name;
        int age;
        List<String> telNumbers;

        public Person() {
        }

        public String getName() {
            return this.name;
        }

        public int getAge() {
            return this.age;
        }

        public List<String> getTelNumbers() {
            return this.telNumbers;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public void setTelNumbers(List<String> telNumbers) {
            this.telNumbers = telNumbers;
        }

        public String toString() {
            return "JacksonBeansTest.Person(name=" + this.getName() + ", age=" + this.getAge() + ", telNumbers=" + this.getTelNumbers() + ")";
        }
    }
}
