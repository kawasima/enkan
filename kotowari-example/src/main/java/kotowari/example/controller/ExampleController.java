package kotowari.example.controller;

import enkan.collection.Parameters;
import enkan.data.HttpResponse;
import kotowari.component.TemplateEngine;

import javax.inject.Inject;

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
