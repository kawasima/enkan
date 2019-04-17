package enkan.component.thymeleaf;

import enkan.data.HttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.dialect.IDialect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static enkan.util.BeanBuilder.builder;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author kawasima
 */
public class ThymeleafTemplateEngineTest {
    ThymeleafTemplateEngine engine;

    @BeforeEach
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
    public void test2() {
        Locale.setDefault(Locale.ENGLISH);
        HttpResponse response = engine.render("test1", "name", "kawasima", "message", "hello");
        System.out.println(response.getBodyAsString());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void customFunction() {
        HttpResponse response = engine.render("test2", "func", (Function<List, Object>) list -> list.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.joining("&")));
        try (BufferedReader reader = new BufferedReader(new InputStreamReader((InputStream) response.getBody()))) {
            String result = reader.lines().collect(Collectors.joining("\n"));
            assertTrue(result.contains("<span>abc&amp;def</span>"));
        } catch (IOException e) {
            fail("IOException occurred");
        }
    }

    @Test
    public void configure() {
        Set<IDialect> dialects = new HashSet<>();
        engine = builder(new ThymeleafTemplateEngine())
                .set(ThymeleafTemplateEngine::setDialects, dialects)
                .build();
        engine.lifecycle().start(engine);
        HttpResponse response = engine.render("test1", "name", "kawasima", "message", "hello");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader((InputStream) response.getBody()))) {
            String result = reader.lines().collect(Collectors.joining("\n"));
            System.out.println(result);
            assertTrue(result.contains("<span th:text=\"${name}\">"));
        } catch (IOException e) {
            fail("IOException occurred");
        }
    }

    @AfterEach
    public void tearDown() {
        engine.lifecycle().stop(engine);
    }
}
