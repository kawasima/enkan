package enkan.component.s2util.beans;

import enkan.component.BeansConverter;
import enkan.system.EnkanSystem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

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

    public static class Foo implements Serializable {
        int a;
        long b;
        short c;
        String d;

        public Foo() {
        }

        public int getA() {
            return this.a;
        }

        public long getB() {
            return this.b;
        }

        public short getC() {
            return this.c;
        }

        public String getD() {
            return this.d;
        }

        public void setA(int a) {
            this.a = a;
        }

        public void setB(long b) {
            this.b = b;
        }

        public void setC(short c) {
            this.c = c;
        }

        public void setD(String d) {
            this.d = d;
        }

        public boolean equals(Object o) {
            if (o == this) return true;
            if (!(o instanceof Foo)) return false;
            final Foo other = (Foo) o;
            if (!other.canEqual(this)) return false;
            if (this.getA() != other.getA()) return false;
            if (this.getB() != other.getB()) return false;
            if (this.getC() != other.getC()) return false;
            final Object this$d = this.getD();
            final Object other$d = other.getD();
            if (this$d == null ? other$d != null : !this$d.equals(other$d)) return false;
            return true;
        }

        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            result = result * PRIME + this.getA();
            final long $b = this.getB();
            result = result * PRIME + (int) ($b >>> 32 ^ $b);
            result = result * PRIME + this.getC();
            final Object $d = this.getD();
            result = result * PRIME + ($d == null ? 43 : $d.hashCode());
            return result;
        }

        protected boolean canEqual(Object other) {
            return other instanceof Foo;
        }

        public String toString() {
            return "S2BeanConverterTest.Foo(a=" + this.getA() + ", b=" + this.getB() + ", c=" + this.getC() + ", d=" + this.getD() + ")";
        }
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
            return "S2BeanConverterTest.TestBean(name=" + this.getName() + ", age=" + this.getAge() + ", address=" + this.getAddress() + ")";
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
            return "S2BeanConverterTest.Person(name=" + this.getName() + ", age=" + this.getAge() + ", telNumbers=" + this.getTelNumbers() + ")";
        }
    }

}
