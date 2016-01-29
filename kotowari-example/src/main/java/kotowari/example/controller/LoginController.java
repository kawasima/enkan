package kotowari.example.controller;

import enkan.component.DomaDaoProvider;
import enkan.data.HttpResponse;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

/**
 * @author kawasima
 */
public class LoginController {
    @Inject
    private DomaDaoProvider daoProvider;

    public HttpResponse login(Map<String, List<String>> params) {
        return HttpResponse.of(params.get("account").get(0));
    }
}
