package enkan.component.s2util.beans;

import lombok.Data;
import org.junit.Test;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;

public class S2BeanConverterTest {
    @Data
    public static class Foo implements Serializable {
        int a;
        long b;
        short c;
        String d;
    }

    @Test
    public void test() {
        HashMap m = new HashMap<>();
        m.put("a", "1");
        m.put("b", "23456");
        m.put("c", "1");
        m.put("d", Arrays.asList("a", "b"));

        Foo foo = new S2BeansConverter().createFrom(m, Foo.class);
        System.out.println(foo);
    }
}
