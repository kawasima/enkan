package enkan.adapter;

import enkan.application.WebApplication;
import enkan.collection.Headers;
import enkan.collection.OptionMap;
import enkan.data.DefaultHttpRequest;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.exception.MisconfigurationException;
import enkan.exception.ServiceUnavailableException;
import enkan.exception.UnreachableException;
import io.undertow.Undertow;
import io.undertow.io.IoCallback;
import io.undertow.io.Sender;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.HttpString;
import org.xnio.streams.ChannelInputStream;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.security.*;

/**
 * Undertow exchange adapter.
 *
 * @author kawasima
 */
public class UndertowAdapter {
    private static IoCallback callback = new IoCallback() {
        @Override
        public void onComplete(HttpServerExchange exchange, Sender sender) {

        }

        @Override
        public void onException(HttpServerExchange exchange, Sender sender, IOException exception) {

        }
    };

    private static void setBody(Sender sender, Object body) throws IOException {
        if (body == null) {
            return; // Do nothing
        }

        if (body instanceof String) {
            sender.send((String) body);
        } else if (body instanceof InputStream) {
            ReadableByteChannel chan = Channels.newChannel((InputStream) body);

            ByteBuffer buf = ByteBuffer.allocate(4096);
            for (;;) {
                int size = chan.read(buf);
                if (size <= 0) break;
                buf.flip();
                sender.send(buf, callback);
                buf.clear();
            }
            sender.close(IoCallback.END_EXCHANGE);
        } else if (body instanceof File) {
            try(FileInputStream fis = new FileInputStream((File) body);
                FileChannel chan = fis.getChannel()) {
                ByteBuffer buf = ByteBuffer.allocate(4096);
                for (;;) {
                    int size = chan.read(buf);
                    if (size <= 0) break;
                    buf.flip();
                    sender.send(buf, callback);
                    buf.clear();
                }
                sender.close(IoCallback.END_EXCHANGE);
            }
        } else {
            throw new UnreachableException();
        }
    }

    private void setResponseHeaders(Headers headers, HttpServerExchange exchange) {
        HeaderMap map = exchange.getResponseHeaders();
        headers.keySet().forEach(headerName -> headers.getList(headerName)
                .forEach(v -> {
                    if (v instanceof String) {
                        map.add(HttpString.tryFromString(headerName), (String) v);
                    } else if (v instanceof Number) {
                        map.add(HttpString.tryFromString(headerName), ((Number) v).longValue());
                    }
                }));
    }

    private void setOptions(Undertow.Builder builder, OptionMap options) {
        if (options.containsKey("ioThreads")) builder.setIoThreads(options.getInt("ioThreads"));
        if (options.containsKey("workerThreads")) builder.setWorkerThreads(options.getInt("workerThreads"));
        if (options.containsKey("bufferSize")) builder.setBufferSize(options.getInt("bufferSize"));
        if (options.containsKey("directBuffers")) builder.setDirectBuffers(options.getBoolean("directBuffers"));
    }

    public Undertow runUndertow(WebApplication application, OptionMap options) {
        Undertow.Builder builder = Undertow.builder();

        builder.setHandler(new HttpHandler() {
            @Override
            public void handleRequest(HttpServerExchange exchange) throws Exception {
                if (exchange.isInIoThread()) {
                    exchange.dispatch(this);
                    return;
                }
                HttpRequest request = new DefaultHttpRequest();
                request.setRequestMethod(exchange.getRequestMethod().toString());
                request.setUri(exchange.getRequestURI());
                request.setProtocol(exchange.getProtocol().toString());
                request.setQueryString(exchange.getQueryString());
                request.setCharacterEncoding(exchange.getRequestCharset());
                request.setBody(new ChannelInputStream(exchange.getRequestChannel()));
                request.setContentLength(exchange.getRequestContentLength());
                request.setRemoteAddr(exchange.getSourceAddress().toString());
                request.setScheme(exchange.getRequestScheme());
                request.setServerName(exchange.getHostName());
                request.setServerPort(exchange.getHostPort());
                Headers headers = Headers.empty();
                exchange.getRequestHeaders().forEach(e -> {
                    String headerName = e.getHeaderName().toString();
                    e.forEach(v -> headers.put(headerName, v));
                });
                request.setHeaders(headers);

                try {
                    HttpResponse response = application.handle(request);
                    exchange.setStatusCode(response.getStatus());
                    setResponseHeaders(response.getHeaders(), exchange);

                    exchange.startBlocking();
                    setBody(exchange.getResponseSender(), response.getBody());
                } catch (ServiceUnavailableException ex) {
                    exchange.setStatusCode(503);
                } finally {
                    exchange.endExchange();
                }
            }
        });

        setOptions(builder, options);
        if (options.getBoolean("http?", true)) {
            builder.addHttpListener(options.getInt("port", 80),
                    options.getString("host", "0.0.0.0"));
        }

        if (options.getBoolean("ssl?", false)) {
            builder.addHttpsListener(options.getInt("sslPort", 443),
                    options.getString("host", "0.0.0.0"),
                    createSslContext(options));
        }
        Undertow undertow = builder.build();
        undertow.start();

        return undertow;
    }

    private SSLContext createSslContext(OptionMap options) {
        try {
            SSLContext context = SSLContext.getInstance("TLSv1.2");
            KeyManager[] keyManagers = null;
            TrustManager[] trustManagers = null;

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            KeyStore keystore = (KeyStore) options.get("keystore");
            if (keystore != null) {
                kmf.init(keystore, options.getString("keystorePassword", "").toCharArray());
                keyManagers = kmf.getKeyManagers();
            }

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            KeyStore truststore = (KeyStore) options.get("truststore");
            if (truststore != null) {
                tmf.init(truststore);
                trustManagers = tmf.getTrustManagers();
            }

            context.init(keyManagers, trustManagers, null);
            return context;
        } catch (UnrecoverableKeyException e) {
            throw new MisconfigurationException("core.UNRECOVERABLE_KEY", e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            // This cannot be thrown because it use only DefaultAlgorithm.
            throw new UnreachableException(e);
        } catch (KeyStoreException e) {
            throw new MisconfigurationException("core.KEY_STORE", e.getMessage(), e);
        } catch (KeyManagementException e) {
            throw new MisconfigurationException("core.KEY_MANAGEMENT", e.getMessage(), e);
        }
    }
}
