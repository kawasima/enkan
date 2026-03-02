package enkan.component.jackson;

import enkan.component.BeansConverter;
import enkan.exception.MisconfigurationException;
import enkan.system.EnkanSystem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author kawasima
 */
public class JacksonBeansTest {
    private EnkanSystem system;
    private BeansConverter beansConverter;

    @BeforeEach
    public void setUp() {
        system = EnkanSystem.of("beans", new JacksonBeansConverter());
        system.start();
        beansConverter = system.getComponent("beans");
    }

    @AfterEach
    public void tearDown() {
        system.stop();
    }

    // ---- copy: REPLACE_ALL ----

    @Test
    public void copyReplaceAllOverwritesAllFields() {
        TestBean source = new TestBean("Alice", "30", "Tokyo");
        Person dest = new Person();
        dest.setName("OldName");

        beansConverter.copy(source, dest, BeansConverter.CopyOption.REPLACE_ALL);

        assertThat(dest.getName()).isEqualTo("Alice");
    }

    @Test
    public void copyDefaultIsReplaceAll() {
        TestBean source = new TestBean(null, "10", "TOKYO");
        Person dest = new Person();
        dest.setName("GGG");

        beansConverter.copy(source, dest);

        assertThat(dest.getName()).isNull();
    }

    // ---- copy: REPLACE_NON_NULL ----

    @Test
    public void copyReplaceNonNullPreservesDestinationWhenSourceIsNull() {
        TestBean source = new TestBean(null, "10", "TOKYO");
        Person dest = new Person();
        dest.setName("GGG");

        beansConverter.copy(source, dest, BeansConverter.CopyOption.REPLACE_NON_NULL);

        assertThat(dest.getName()).isEqualTo("GGG");
    }

    @Test
    public void copyReplaceNonNullOverwritesWhenSourceIsNotNull() {
        TestBean source = new TestBean("NewName", "10", "TOKYO");
        Person dest = new Person();
        dest.setName("OldName");

        beansConverter.copy(source, dest, BeansConverter.CopyOption.REPLACE_NON_NULL);

        assertThat(dest.getName()).isEqualTo("NewName");
    }

    @Test
    public void copyReplaceNonNullIsIdempotentOnSecondCallWithReplaceAll() {
        TestBean source = new TestBean(null, "10", "TOKYO");
        Person dest = new Person();
        dest.setName("GGG");

        beansConverter.copy(source, dest, BeansConverter.CopyOption.REPLACE_NON_NULL);
        assertThat(dest.getName()).isEqualTo("GGG");

        beansConverter.copy(source, dest);
        assertThat(dest.getName()).isNull();
    }

    // ---- copy: PRESERVE_NON_NULL ----

    @Test
    public void copyPreserveNonNullThrowsMisconfigurationException() {
        TestBean source = new TestBean("Alice", "30", "Tokyo");
        Person dest = new Person();

        assertThatThrownBy(() -> beansConverter.copy(source, dest, BeansConverter.CopyOption.PRESERVE_NON_NULL))
                .isInstanceOf(MisconfigurationException.class);
    }

    // ---- createFrom ----

    @Test
    public void createFromBeanToBean() {
        TestBean source = new TestBean("Bob", "25", "Osaka");

        Person person = beansConverter.createFrom(source, Person.class);

        assertThat(person.getName()).isEqualTo("Bob");
    }

    @Test
    public void createFromMapToBean() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "Jackson");
        map.put("age", 10);

        Person person = beansConverter.createFrom(map, Person.class);

        assertThat(person.getName()).isEqualTo("Jackson");
        assertThat(person.getAge()).isEqualTo(10);
    }

    @Test
    public void createFromIllegalArgumentWhenDestinationClassIsNull() {
        Person source = new Person();
        assertThatThrownBy(() -> beansConverter.createFrom(source, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void createFromIllegalArgumentForPrimitiveWrappers() {
        Person source = new Person();
        source.setName("GGG");
        source.setAge(10);

        assertThatThrownBy(() -> beansConverter.createFrom(source, String.class))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> beansConverter.createFrom(source, BigDecimal.class))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void createFromIllegalArgumentForCollectionAndArray() {
        Person source = new Person();

        assertThatThrownBy(() -> beansConverter.createFrom(source, ArrayList.class))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> beansConverter.createFrom(source, Object[].class))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ---- JavaTimeModule ----

    @Test
    public void javaTimeModuleSerializesLocalDateAsIsoString() {
        DateBean source = new DateBean();
        source.setBirthday(LocalDate.of(1990, 5, 15));

        DateBean dest = new DateBean();
        beansConverter.copy(source, dest, BeansConverter.CopyOption.REPLACE_ALL);

        assertThat(dest.getBirthday()).isEqualTo(LocalDate.of(1990, 5, 15));
    }

    // ---- restart ----

    @Test
    public void componentWorksAfterRestart() {
        system.stop();
        system.start();
        beansConverter = system.getComponent("beans");

        TestBean source = new TestBean("Restart", "1", "X");
        Person dest = new Person();
        beansConverter.copy(source, dest);

        assertThat(dest.getName()).isEqualTo("Restart");
    }

    // ---- inner test classes ----

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

        public String getName() { return name; }
        public String getAge() { return age; }
        public String getAddress() { return address; }
        public void setName(String name) { this.name = name; }
        public void setAge(String age) { this.age = age; }
        public void setAddress(String address) { this.address = address; }
    }

    public static class Person implements Serializable {
        String name;
        int age;
        List<String> telNumbers;

        public Person() {}

        public String getName() { return name; }
        public int getAge() { return age; }
        public List<String> getTelNumbers() { return telNumbers; }
        public void setName(String name) { this.name = name; }
        public void setAge(int age) { this.age = age; }
        public void setTelNumbers(List<String> telNumbers) { this.telNumbers = telNumbers; }
    }

    public static class DateBean implements Serializable {
        private LocalDate birthday;

        public LocalDate getBirthday() { return birthday; }
        public void setBirthday(LocalDate birthday) { this.birthday = birthday; }
    }
}
