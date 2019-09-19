package enkan.util;

import enkan.exception.MisconfigurationException;
import org.junit.jupiter.api.Test;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import static org.assertj.core.api.Assertions.*;

/**
 * @author kawasima
 */
class BeanBuilderTest {
    @Test
    void builder() {
        assertThatThrownBy(() -> BeanBuilder.builder(new Person())
                .set(Person::setName, "kawasima")
                .set(Person::setAge, 3)
                .build())
                .isInstanceOf(MisconfigurationException.class);
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
