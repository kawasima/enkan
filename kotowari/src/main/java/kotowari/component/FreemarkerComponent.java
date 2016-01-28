package kotowari.component;

import enkan.component.ComponentLifecycle;
import enkan.component.SystemComponent;
import enkan.data.HttpResponse;
import enkan.exception.FalteringEnvironmentException;
import enkan.exception.MisconfigurationException;
import enkan.exception.UnreachableException;
import enkan.exception.UnrecoverableException;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;
import kotowari.data.TemplatedHttpResponse;
import kotowari.io.LazyRenderInputStream;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;

/**
 * @author kawasima
 */
public class FreemarkerComponent extends TemplateEngineComponent {
    private Configuration config;
    private String prefix = "templates";
    private String suffix = ".ftl";
    private ClassLoader classLoader;
    private String encoding = "UTF-8";

    @Override
    public HttpResponse render(String name, Object... keyOrVals) {
        TemplatedHttpResponse response = TemplatedHttpResponse.create(name, keyOrVals);
        response.setBody(new LazyRenderInputStream(() -> {
            try {
                Template template = config.getTemplate(name + suffix, encoding);
                StringWriter writer = new StringWriter();
                template.process(response.getContext(), writer);
                return new ByteArrayInputStream(writer.toString().getBytes(encoding));
            } catch (TemplateException e) {
                MisconfigurationException.raise("RENDERING_ERROR", e);
                throw UnreachableException.create();
            } catch (IOException e) {
                throw FalteringEnvironmentException.create(e);
            }

        }));
        return response;
    }

    @Override
    protected ComponentLifecycle lifecycle() {
        return new ComponentLifecycle() {
            @Override
            public void start(SystemComponent component) {
                if (classLoader == null) {
                    classLoader = Thread.currentThread().getContextClassLoader();
                }
                config = new Configuration(new Version(2,3,23));
                ClassTemplateLoader classTemplateLoader = new ClassTemplateLoader(classLoader, prefix);
                config.setTemplateLoader(classTemplateLoader);
            }

            @Override
            public void stop(SystemComponent component) {
                config = null;
                classLoader = null;
            }
        };
    }
}
