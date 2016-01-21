package kotowari.application;

import enkan.application.WebApplication;
import kotowari.routing.Routes;

/**
 * @author kawasima
 */
public class KotowariApplication extends WebApplication {
    private Routes routes;

    public KotowariApplication(Routes routes) {
        this.routes = routes;
    }

    public Routes getRoutes() {
        return routes;
    }
}
