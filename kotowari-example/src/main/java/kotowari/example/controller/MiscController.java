package kotowari.example.controller;

import enkan.collection.Parameters;
import enkan.data.HttpResponse;
import kotowari.component.TemplateEngine;

import javax.inject.Inject;
import java.io.File;

/**
 * @author kawasima
 */
public class MiscController {
    @Inject
    private TemplateEngine templateEngine;

    public HttpResponse uploadForm() {
        return templateEngine.render("misc/upload");
    }

    public String upload(Parameters params) {
        File tempfile = (File) params.getIn("datafile", "tempfile");
        return tempfile.getAbsolutePath() + "("
                + tempfile.length()
                + " bytes) is uploaded. description: "
                + params.get("description");
    }
}
