package enkan.component.freemarker;

import enkan.data.HttpResponse;
import enkan.system.EnkanSystem;
import freemarker.cache.FileTemplateLoader;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.assertj.core.api.Assertions.assertThat;

public class FreemarkerTemplateEngineTest {
    @Test
    public void test() throws IOException {
        EnkanSystem system = EnkanSystem.of("freemarker", new FreemarkerTemplateEngine());
        FreemarkerTemplateEngine freemarker = system.getComponent("freemarker");
        FileTemplateLoader loader = new FileTemplateLoader(new File("src/test/resources"));
        freemarker.setTemplateLoader(loader);
        system.start();

        HttpResponse<InputStream> response = freemarker.render("file/hello");
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(response.getBody()))) {
            String line = reader.readLine();
            assertThat(line).isEqualTo("Hello, Freemarker");
        }

    }
}
