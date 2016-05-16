package enkan.component.thymeleaf;

import enkan.data.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * @author kawasima
 */
public class ThymeleafTemplateEngineTest {
    ThymeleafTemplateEngine engine;

    @Before
    public void setup() {
        engine = new ThymeleafTemplateEngine();
        engine.lifecycle().start(engine);
    }

    @Test
    public void test() {
        HttpResponse response = engine.render("test1", "name", "kawasima", "message", "hello");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader((InputStream) response.getBody()))) {
            assertTrue(reader.lines().anyMatch(s -> s.contains("<span>kawasima</span>")));
        } catch (IOException e) {
            fail("IOException occurred");
        }
    }

    @Test
    public void customFunction() {
        HttpResponse response = engine.render("test2", "func", (Function<List, Object>) list -> list.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.joining("&")));
        try (BufferedReader reader = new BufferedReader(new InputStreamReader((InputStream) response.getBody()))) {
            //assertTrue(reader.lines().anyMatch(s -> s.contains("<span>kawasima</span>")));
            reader.lines().forEach(System.out::println);
        } catch (IOException e) {
            fail("IOException occurred");
        }
    }

    @After
    public void tearDown() {
        engine.lifecycle().stop(engine);
    }
}
