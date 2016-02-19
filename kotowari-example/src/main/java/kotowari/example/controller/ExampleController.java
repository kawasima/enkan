package kotowari.example.controller;

import enkan.collection.Parameters;
import enkan.data.HttpResponse;
import enkan.data.Session;
import kotowari.component.TemplateEngine;

import javax.inject.Inject;

import static enkan.util.BeanBuilder.builder;
import static enkan.util.HttpResponseUtils.response;

/**
 * @author kawasima
 */
public class ExampleController {
    @Inject
    private TemplateEngine templateEngine;

    public HttpResponse index() {
        return templateEngine.render("index");
    }

    public String method2(Parameters params) {
        return "method2です " + params.get("name");
    }
}
