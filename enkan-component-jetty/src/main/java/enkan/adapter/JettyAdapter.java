package enkan.adapter;

import enkan.application.WebApplication;
import enkan.collection.OptionMap;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.exception.FalteringEnvironmentException;
import enkan.exception.MisconfigurationException;
import enkan.util.ServletUtils;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.KeyStore;
import java.util.function.BiFunction;

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

    private SslContextFactory createSslContextFactory(OptionMap options) {
        SslContextFactory context = new SslContextFactory();
        Object keystore = options.get("keystore");
        if (keystore instanceof KeyStore) {
            context.setKeyStore((KeyStore) keystore);
        } else {
            throw new MisconfigurationException("");
        }
        context.setKeyStorePassword(options.getString("keystorePassword"));

        Object truststore = options.get("truststore");
         if (truststore instanceof KeyStore) {
            context.setTrustStore((KeyStore) truststore);
        }
        context.setTrustStorePassword(options.getString("truststorePassword"));

        String clientAuth = options.getString("clientAuth", "none");
        switch (clientAuth) {
            case "need": context.setNeedClientAuth(true); break;
            case "want": context.setWantClientAuth(true); break;
        }

        return context;
    }

    private ServerConnector createSslConnector(Server server, OptionMap options) {
        int sslPort = options.getInt("sslPort", 443);
        HttpConfiguration config = httpConfiguration(options);
        config.setSecureScheme("https");
        config.setSecurePort(sslPort);
        config.addCustomizer(new SecureRequestCustomizer());
        HttpConnectionFactory httpFactory = new HttpConnectionFactory(config);

        SslConnectionFactory sslFactory = new SslConnectionFactory(createSslContextFactory(options), "http/1.1");

        ServerConnector connector = new ServerConnector(server, sslFactory, httpFactory);
        connector.setPort(sslPort);
        connector.setHost(options.getString("host"));
        connector.setIdleTimeout(options.getInt("maxIdleTime", 200000));
        return connector;
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

        BiFunction<Server, OptionMap, ServerConnector> serverConnectorFactory = (BiFunction<Server, OptionMap, ServerConnector>) options.get("serverConnectorFactory");
        if (serverConnectorFactory != null) {
            Connector connector = serverConnectorFactory.apply(server, options);
            server.addConnector(connector);
        } else if (options.getBoolean("http?", true)) {
            server.addConnector(createHttpConnector(server, options));
        }

        if (options.getBoolean("ssl?", false)) {
            server.addConnector(createSslConnector(server, options));
        }

        return server;
    }

    public Server runJetty(WebApplication application) {
        return runJetty(application, OptionMap.of());
    }

    public Server runJetty(WebApplication application, OptionMap options) {
        Server server = createServer(options);
        server.setHandler(new ProxyHandler(application));
        try {
            server.setStopAtShutdown(true);
            server.setStopTimeout(3000);
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
