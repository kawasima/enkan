package kotowari.example.controller;

import enkan.data.HttpResponse;
import enkan.exception.UnreachableException;
import kotowari.component.TemplateEngine;

import javax.inject.Inject;

/**
 * @author kawasima
 */
public class HospitalityDemoController {
    @Inject
    private TemplateEngine templateEngine;

    public String unreachable() {
        throw UnreachableException.create();
    }

    public HttpResponse misconfiguration() {
        return templateEngine.render("misc/misconfiguration");
    }
}
