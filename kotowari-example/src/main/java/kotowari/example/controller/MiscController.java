package kotowari.example.controller;

import enkan.collection.Parameters;
import enkan.data.HttpResponse;
import enkan.data.Session;
import kotowari.component.TemplateEngine;

import javax.inject.Inject;
import java.io.File;

import static enkan.util.BeanBuilder.builder;
import static enkan.util.HttpResponseUtils.response;

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

    public HttpResponse counter(Session session) {
        int count = 0;
        if (session != null) {
            count = (Integer) session.get("count");
            count++;
        } else {
            session = new Session();
        }
        session.put("count", count);
        return builder(response(count + "times."))
                .set(HttpResponse::setContentType, "text/plain")
                .set(HttpResponse::setSession, session)
                .build();

    }

}
