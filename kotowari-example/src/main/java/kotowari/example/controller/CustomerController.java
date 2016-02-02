package kotowari.example.controller;

import enkan.data.HttpResponse;
import kotowari.component.TemplateEngineComponent;
import kotowari.example.form.CustomerForm;

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

    public HttpResponse create(CustomerForm form) {
        return templateEngine.render("customer/create");
    }
}
