package enkan.component.thymeleaf;

import enkan.component.ComponentLifecycle;
import enkan.data.HttpResponse;
import enkan.exception.MisconfigurationException;
import enkan.util.HttpResponseUtils;
import kotowari.component.TemplateEngine;
import kotowari.data.TemplatedHttpResponse;
import kotowari.io.LazyRenderInputStream;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

/**
 * @author kawasima
 */
public class ThymeleafTemplateEngine extends TemplateEngine {
    private String prefix = "templates/";
    private String suffix = ".html";
    private ClassLoader classLoader;
    private String encoding = "UTF-8";

    org.thymeleaf.TemplateEngine thymeleafEngine;

    @Override
    public HttpResponse render(String name, Object... keyOrVals) {



        TemplatedHttpResponse response = TemplatedHttpResponse.create(name, keyOrVals);
        response.setBody(new LazyRenderInputStream(() -> {
            // FIXME set locale.
            Context ctx = new Context(Locale.US, response.getContext());

            try {
                return new ByteArrayInputStream(thymeleafEngine.process(name, ctx).getBytes(encoding));
            } catch (UnsupportedEncodingException e) {
                throw new MisconfigurationException("core.UNSUPPORTED_ENCODING", encoding, e);
            }
        }));

        HttpResponseUtils.contentType(response, "text/html");
        return response;
    }

    @Override
    public Object createFunction(Function<List, Object> func) {
        return func;
    }

    @Override
    protected ComponentLifecycle<ThymeleafTemplateEngine> lifecycle() {
        return new ComponentLifecycle<ThymeleafTemplateEngine>() {
            @Override
            public void start(ThymeleafTemplateEngine component) {
                if (classLoader == null) {
                    classLoader = Thread.currentThread().getContextClassLoader();
                }
                ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver(classLoader);
                resolver.setTemplateMode(TemplateMode.HTML);
                resolver.setPrefix(prefix);
                resolver.setSuffix(suffix);
                resolver.setCharacterEncoding(encoding);

                component.thymeleafEngine = new org.thymeleaf.TemplateEngine();
                component.thymeleafEngine.setTemplateResolver(resolver);
            }

            @Override
            public void stop(ThymeleafTemplateEngine component) {
                component.classLoader = null;
                component.thymeleafEngine = null;
            }
        };
    }
}
