package kotowari.scaffold.command;

import enkan.Application;
import enkan.application.WebApplication;
import enkan.collection.OptionMap;
import enkan.component.ApplicationComponent;
import enkan.component.hikaricp.HikariCPComponent;
import enkan.config.ApplicationFactory;
import enkan.system.EnkanSystem;
import enkan.system.inject.ComponentInjector;
import net.unit8.amagicman.PathResolver;
import net.unit8.amagicman.PathResolverImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static enkan.component.ComponentRelationship.component;


/**
 * @author kawasima
 */
public class ScaffoldCommandTest {
    private PathResolver pathResolver = new PathResolverImpl(null, "META-INF/amagicman/templates",
            "target/test-work");

    public static class TestApplicationFactory implements ApplicationFactory {
        @Override
        public Application create(ComponentInjector injector) {
            return new WebApplication();
        }
    }

    @BeforeEach
    void createApplicationFactory() throws IOException, URISyntaxException {
        try (OutputStream out = pathResolver.destinationAsStream("src/main/java/kotowari/scaffold/MyApplicationFactory.java")) {
            Files.copy(Paths.get("src/test/java/kotowari/scaffold/MyApplicationFactory.java"), out);
        }
    }

    @Test
    void test() {
        ReplMock repl = new ReplMock();
        TransportMock transport = new TransportMock();
        EnkanSystem system = EnkanSystem.of(
                "app", new ApplicationComponent("kotowari.scaffold.MyApplicationFactory"),
                "datasource", new HikariCPComponent(OptionMap.of("uri", "jdbc:h2:mem:test"))
        ).relationships(
                component("app").using("datasource")
        );
        system.start();
        ScaffoldCommandRegister commandRegister = new ScaffoldCommandRegister();
        commandRegister.setPathResolver(pathResolver);
        commandRegister.register(repl);

        repl.getCommand("generate").execute(system, transport, "table", "hoge(",
                "id BIGINT PRIMARY KEY, name VARCHAR(255) NOT NULL)");
        repl.getCommand("generate").execute(system, transport, "crud", "Hoge");
    }
}
