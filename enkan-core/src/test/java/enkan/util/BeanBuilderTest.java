package enkan.util;

import enkan.exception.MisconfigurationException;
import org.junit.Test;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import static org.junit.Assert.*;

/**
 * @author kawasima
 */
public class BeanBuilderTest {
    @Test
    public void builder() {
        try {
            Person p1 = BeanBuilder.builder(new Person())
                    .set(Person::setName, "kawasima")
                    .set(Person::setAge, 3)
                    .build();
            fail("MisconfigurationException occur");
        } catch (MisconfigurationException ex) {

        }
    }

    static class Person {
        @NotNull
        private String name;

        @DecimalMin("10")
        private int age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }

}
