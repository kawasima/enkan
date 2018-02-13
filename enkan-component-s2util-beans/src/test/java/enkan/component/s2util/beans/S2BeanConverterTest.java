package enkan.component.s2util.beans;

import enkan.component.BeansConverter;
import enkan.system.EnkanSystem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class S2BeanConverterTest {
    private EnkanSystem system;

    @BeforeEach
    public void setUp() {
        system = EnkanSystem.of("beans", new S2BeansConverter());
        system.start();
    }

    @AfterEach
    public void tearDown() {
        system.stop();
    }

    @Data
    public static class Foo implements Serializable {
        int a;
        long b;
        short c;
        String d;
    }

    @Test
    public void test() {
        HashMap<String, Object> m = new HashMap<>();
        m.put("a", "1");
        m.put("b", "23456");
        m.put("c", "1");
        m.put("d", Arrays.asList("a", "b"));
        S2BeansConverter beans = system.getComponent("beans");

        Foo foo = beans.createFrom(m, Foo.class);
        assertThat(foo.getA()).isEqualTo(1);
        assertThat(foo.getB()).isEqualTo(23456);
        assertThat(foo.getC()).isEqualTo((short) 1);
        assertThat(foo.getD()).isEqualTo("[a, b]");
    }

    @Test
    public void testNull() {
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
