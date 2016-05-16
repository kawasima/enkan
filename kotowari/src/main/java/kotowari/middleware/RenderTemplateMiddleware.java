package kotowari.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.component.builtin.HmacEncoder;
import enkan.data.ConversationState;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.data.PrincipalAvailable;
import enkan.exception.FalteringEnvironmentException;
import enkan.middleware.AbstractWebMiddleware;
import kotowari.data.TemplatedHttpResponse;
import kotowari.scope.ExportSetting;
import static kotowari.scope.ExportableScope.*;

import javax.enterprise.context.Conversation;
import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.stream.Stream;

/**
 * Renders HTML using by template.
 *
 * @author kawasima
 */
@Middleware(name = "renderTemplate")
public class RenderTemplateMiddleware extends AbstractWebMiddleware {
    @Inject
    private HmacEncoder hmacEncoder;

    private ExportSetting exports = ExportSetting.DEFAULT_EXPORTS;

    protected void render(TemplatedHttpResponse response) {
        InputStream is = (InputStream) response.getBody();
        ReadableByteChannel channel = Channels.newChannel(is);
        ByteBuffer buf = ByteBuffer.allocate(4096);
        buf.mark();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            int read;
            while ((read = channel.read(buf)) >= 0) {
                baos.write(buf.array(), 0, read);
                buf.reset();
            }
            String body = baos.toString("UTF-8");
            response.setBody(body);
        } catch (IOException ex) {
            throw new FalteringEnvironmentException(ex);
        }
    }

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain chain) {
        HttpResponse response = castToHttpResponse(chain.next(request));
        if (TemplatedHttpResponse.class.isInstance(response)) {
            TemplatedHttpResponse tres = TemplatedHttpResponse.class.cast(response);
            if (exports.contains(REQUEST)) {
                tres.getContext().put(exports.getExportName(REQUEST), request);
            }

            if (exports.contains(PARAMS)) {
                tres.getContext().put(exports.getExportName(PARAMS), request.getParams());
            }

            if (exports.contains(USER_PRINCIPAL)) {
                Stream.of(request)
                        .filter(PrincipalAvailable.class::isInstance)
                        .map(PrincipalAvailable.class::cast)
                        .findFirst()
                        .ifPresent(principal -> tres.getContext()
                                .put(exports.getExportName(USER_PRINCIPAL), principal.getPrincipal()));
            }
            if (exports.contains(SESSION)) {
                tres.getContext().put(exports.getExportName(SESSION), request.getSession());
            }

            if (exports.contains(CONVERSATION)) {
                Conversation conversation = request.getConversation();
                if (conversation != null && !request.getConversation().isTransient()) {
                    String token = conversation.getId() + "$"
                            + hmacEncoder.encodeToHex(conversation.getId() + "$" + conversation.getTimeout())
                            + "$" + conversation.getTimeout();
                    tres.getContext().put("conversationToken", token);
                }
                tres.getContext().put(exports.getExportName(CONVERSATION), conversation);
            }

            if (exports.contains(CONVERSATION_STATE)) {
                ConversationState conversationState = request.getConversationState();
                tres.getContext().put(exports.getExportName(CONVERSATION_STATE), conversationState);
            }
            render(tres);
        }
        return response;
    }
}
