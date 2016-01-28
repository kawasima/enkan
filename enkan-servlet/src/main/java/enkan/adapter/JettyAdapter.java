package enkan.adapter;

import enkan.application.WebApplication;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.exception.FalteringEnvironmentException;
import enkan.exception.UnrecoverableException;
import enkan.collection.OptionMap;
import enkan.util.ServletUtils;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author kawasima
 */
public class JettyAdapter {
    private static class ProxyHandler extends AbstractHandler {
        private WebApplication application;
        ProxyHandler(WebApplication application) {
            this.application = application;
        }

        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            HttpRequest httpRequest = ServletUtils.buildRequest(request);
            HttpResponse httpResponse = application.handle(httpRequest);
            ServletUtils.updateServletResponse(response, httpResponse);
        }
    }

    private ServerConnector createHttpConnector(Server server, OptionMap options) {
        HttpConnectionFactory factory = new HttpConnectionFactory();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(options.getInt("port", 80));
        connector.setHost(options.getString("host"));
        connector.setIdleTimeout(options.getLong("maxIdleTime", 200000));
        return connector;
    }
    private ThreadPool createThreadPool() {
        QueuedThreadPool pool = new QueuedThreadPool(50);
        pool.setMinThreads(8);
        return pool;
    }

    private Server createServer(OptionMap options) {
        Server server = new Server(createThreadPool());
        server.addConnector(createHttpConnector(server, options));
        return server;
    }

    public Server runJetty(WebApplication application) {
        return runJetty(application, OptionMap.of());
    }

    public Server runJetty(WebApplication application, OptionMap options) {
        Server server = createServer(options);
        server.setHandler(new ProxyHandler(application));
        try {
            server.start();
            if (options.getBoolean("join?", true)) {
                server.join();
            }
        } catch (Exception ex) {
            try {
                server.stop();
                FalteringEnvironmentException.create(ex);
            } catch (Exception stopEx) {
                FalteringEnvironmentException.create(stopEx);
            }
        }
        return server;
    }
}
