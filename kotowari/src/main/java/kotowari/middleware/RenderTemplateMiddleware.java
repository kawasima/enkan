package kotowari.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.exception.FalteringEnvironmentException;
import enkan.middleware.AbstractWebMiddleware;
import kotowari.data.TemplatedHttpResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * @author kawasima
 */
@Middleware(name = "renderTemplate")
public class RenderTemplateMiddleware extends AbstractWebMiddleware {
    protected void render(TemplatedHttpResponse response) {
        InputStream is = (InputStream) response.getBody();
        ReadableByteChannel channel = Channels.newChannel(is);
        ByteBuffer buf = ByteBuffer.allocate(4096);
        buf.mark();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            int readed;
            while ((readed = channel.read(buf)) >= 0) {
                baos.write(buf.array(), 0, readed);
                buf.reset();
            }
            String body = baos.toString("UTF-8");
            response.setBody(body);
        } catch (IOException ex) {
            throw FalteringEnvironmentException.create(ex);
        }
    }

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain chain) {
        HttpResponse response = castToHttpResponse(chain.next(request));
        if (TemplatedHttpResponse.class.isInstance(response)) {
            render((TemplatedHttpResponse) response);
        }
        return response;
    }
}
