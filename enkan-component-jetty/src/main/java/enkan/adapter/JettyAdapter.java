package enkan.adapter;

import enkan.application.WebApplication;
import enkan.collection.OptionMap;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.exception.FalteringEnvironmentException;
import enkan.util.ServletUtils;
import org.eclipse.jetty.server.*;
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

    private HttpConfiguration httpConfiguration(OptionMap options) {
        HttpConfiguration config = new HttpConfiguration();
        config.setSendDateHeader(options.getBoolean("sendDateHeader", true));
        config.setOutputBufferSize(options.getInt("outputBufferSize", 32768));
        config.setRequestHeaderSize(options.getInt("requestHeaderSize", 8192));
        config.setResponseHeaderSize(options.getInt("responseHeaderSize", 8192));
        config.setSendServerVersion(options.getBoolean("sendServerVersion", true));
        return config;
    }

    private ServerConnector createHttpConnector(Server server, OptionMap options) {
        HttpConnectionFactory factory = new HttpConnectionFactory(httpConfiguration(options));
        ServerConnector connector = new ServerConnector(server, factory);
        connector.setPort(options.getInt("port", 80));
        connector.setHost(options.getString("host", "0.0.0.0"));
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
                throw new FalteringEnvironmentException(ex);
            } catch (Exception stopEx) {
                throw new FalteringEnvironmentException(stopEx);
            }
        }
        return server;
    }
}
