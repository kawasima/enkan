package enkan.component.freemarker;

import enkan.component.ComponentLifecycle;
import enkan.data.HttpResponse;
import enkan.exception.MisconfigurationException;
import enkan.util.HttpResponseUtils;
import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.core.HTMLOutputFormat;
import freemarker.core.OutputFormat;
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
public class FreemarkerTemplateEngine extends TemplateEngine<FreemarkerTemplateEngine> {
    private Configuration config;
    private String prefix = "templates";
    private String suffix = ".ftl";
    private ClassLoader classLoader;
    private String encoding = "UTF-8";
    private TemplateLoader templateLoader;

    private OutputFormat outputFormat = HTMLOutputFormat.INSTANCE;

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    protected ComponentLifecycle<FreemarkerTemplateEngine> lifecycle() {
        return new ComponentLifecycle<FreemarkerTemplateEngine>() {
            @Override
            public void start(FreemarkerTemplateEngine component) {
                if (classLoader == null) {
                    classLoader = Thread.currentThread().getContextClassLoader();
                }
                config = new Configuration(new Version(2,3,27));
                config.setTemplateLoader(createTemplateLoader());
                config.setOutputFormat(outputFormat);
                config.setObjectWrapper(new DefaultObjectWrapper(new Version(2,3,27)) {
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
            public void stop(FreemarkerTemplateEngine component) {
                config = null;
                classLoader = null;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
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

    private TemplateLoader createTemplateLoader() {
        TemplateLoader classTemplateLoader = new ClassTemplateLoader(classLoader, prefix);
        if (templateLoader != null) {
            if (templateLoader instanceof MultiTemplateLoader) {
                MultiTemplateLoader mtl = (MultiTemplateLoader) templateLoader;
                TemplateLoader[] loaders = new TemplateLoader[mtl.getTemplateLoaderCount() + 1];
                for(int i=0; i < mtl.getTemplateLoaderCount(); i++) {
                    loaders[i] = mtl.getTemplateLoader(i);
                }
                loaders[mtl.getTemplateLoaderCount() + 1] = classTemplateLoader;
                return new MultiTemplateLoader(loaders);

            } else {
                return new MultiTemplateLoader(new TemplateLoader[]{templateLoader, classTemplateLoader});
            }
        } else {
            return classTemplateLoader;
        }
    }

    /**
     * Set an output format used by this freemarker.
     *
     * @param outputFormat FreeMarker output format
     */
    public void setOutputFormat(OutputFormat outputFormat) {
        this.outputFormat = outputFormat;
    }

    /**
     * Set an template loader used by this freemarker.
     *
     * @param templateLoader template loader
     */
    public void setTemplateLoader(TemplateLoader templateLoader) {
        this.templateLoader = templateLoader;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}
