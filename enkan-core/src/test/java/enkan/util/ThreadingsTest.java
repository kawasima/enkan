package enkan.util;


import org.junit.Assert;
import org.junit.Test;

import javax.validation.Validation;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Optional;
import java.util.function.Function;

import static enkan.util.ThreadingUtils.*;
import static org.junit.Assert.assertTrue;

/**
 * @author kawasima
 */
public class ThreadingsTest {
    @Test
    public void test() {
        Optional<String> booleanName = ThreadingUtils.some(System.getenv(),
                (env) -> env.get("HOME1"),
                String::isEmpty,
                Object::toString);
        booleanName.ifPresent(System.out::println);

    }

    @Test
    public void file() {
        String path = "^/hoge";
        Optional<URL> url = ThreadingUtils.some(path, File::new, File::toURI, URI::toURL);
        assertTrue(url.isPresent());
        System.out.println(url.get());
    }

    @Test
    public void urlEncode() {
        String str = "あいうえお";

        Optional<String> encoded = some(str,
                partial(URLEncoder::encode, "UTF-8"));
        assertTrue(encoded.isPresent());
        System.out.println(encoded.get());

        str = null;
        encoded = ThreadingUtils.some(str, s -> URLEncoder.encode(s, "UTF-8"));
        Assert.assertFalse(encoded.isPresent());
    }

    @Test
    public void builder() {
        Function<Person, BeanBuilder<Person>> builder = BeanBuilder.builderWithValidation(Validation.buildDefaultValidatorFactory());
        Person p1 = builder.apply(new Person())
                .set(Person::setName, "kawasima")
                .set(Person::setAge,   3)
                .build();
        System.out.println(p1.getAge());
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
