package enkan.middleware;


import enkan.MiddlewareChain;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.data.SessionAvailable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author kawasima
 */
public class AntiForgeryMiddleware extends AbstractWebMiddleware {
    private static final Logger MISCONFIG_LOG = LoggerFactory.getLogger("enkan.misconfiguration");

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain next) {
        if (request instanceof SessionAvailable) {
            return (HttpResponse) next.next(request);
        } else {
            MISCONFIG_LOG.warn("When you use AntiForgeryMiddleware, you must need a HttpRequest implemented SessionAvailable.");
            return (HttpResponse) next.next(request);
        }
    }
}
