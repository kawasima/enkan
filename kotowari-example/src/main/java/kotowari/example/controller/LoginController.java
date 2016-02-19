package kotowari.example.controller;

import enkan.collection.Parameters;
import enkan.component.DomaProvider;
import enkan.data.HttpResponse;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

/**
 * @author kawasima
 */
public class LoginController {
    @Inject
    private DomaProvider daoProvider;

    public HttpResponse login(Parameters params) {
        return HttpResponse.of(params.get("account"));
    }
}
