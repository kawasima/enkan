package kotowari.example.controller;

import enkan.data.HttpResponse;
import kotowari.component.TemplateEngineComponent;
import kotowari.example.form.ExampleForm;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

/**
 * @author kawasima
 */
public class ExampleController {
    @Inject
    private TemplateEngineComponent templateEngine;

    public String method1() {
        System.out.println(templateEngine);
        return "method1";
    }

    public String method2(Map<String, List<String>> params) {
        System.out.println(params);
        return "!!!method2 " + params.get("name");
    }

    public HttpResponse method3(ExampleForm form) {
        System.out.println(form.getName());
        return templateEngine.render("example",
                "name", form.getName());
    }
}
