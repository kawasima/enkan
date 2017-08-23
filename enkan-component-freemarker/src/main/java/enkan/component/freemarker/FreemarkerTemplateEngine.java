package enkan.component.freemarker;

import enkan.component.ComponentLifecycle;
import enkan.component.SystemComponent;
import enkan.data.HttpResponse;
import enkan.exception.MisconfigurationException;
import enkan.util.HttpResponseUtils;
import freemarker.cache.ClassTemplateLoader;
import freemarker.ext.beans.BeanModel;
import freemarker.ext.beans.StringModel;
import freemarker.template.*;
import kotowari.component.TemplateEngine;
import kotowari.data.TemplatedHttpResponse;
import kotowari.data.Validatable;
import kotowari.io.LazyRenderInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Template engine component using by Freemarker.
 *
 * @author kawasima
 */
public class FreemarkerTemplateEngine extends TemplateEngine {
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
                throw new MisconfigurationException("freemarker.TEMPLATE", e.getFTLInstructionStack(), e.getMessageWithoutStackTop(), e);
            } catch (IOException e) {
                throw new MisconfigurationException("freemarker.TEMPLATE", e.getMessage(),
                        String.format(Locale.US, "Make a template '%s'.", name), e);
            }

        }));
        HttpResponseUtils.contentType(response, "text/html");
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
                config.setObjectWrapper(new DefaultObjectWrapper(new Version(2,3,23)) {
                    @Override
                    protected TemplateModel handleUnknownType(final Object obj) throws TemplateModelException {
                        if (obj instanceof Validatable) {
                            return new ValidatableFormAdapter((Validatable) obj, this);
                        }
                        return super.handleUnknownType(obj);
                    }
                });
            }

            @Override
            public void stop(SystemComponent component) {
                config = null;
                classLoader = null;
            }
        };
    }

    @Override
    public Object createFunction(Function<List, Object> func) {
        return (TemplateMethodModelEx) arguments ->
                func.apply((List)arguments.stream().map(arg -> {
                    if (arg instanceof BeanModel) {
                        return ((StringModel) arg).getWrappedObject();
                    } else {
                        return arg;
                    }
                }).collect(Collectors.toList()));
    }
}
