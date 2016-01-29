package kotowari.example.controller;

import enkan.data.HttpResponse;
import kotowari.component.TemplateEngineComponent;

import javax.inject.Inject;

/**
 * @author kawasima
 */
public class CustomerController {
    @Inject
    private TemplateEngineComponent templateEngine;
    public HttpResponse newForm() {
        return templateEngine.render("customer/new");
    }
}
