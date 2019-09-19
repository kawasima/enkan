package enkan.middleware;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.collection.Headers;
import enkan.component.builtin.HmacEncoder;
import enkan.data.ConversationState;
import enkan.data.DefaultConversation;
import enkan.data.HttpRequest;
import enkan.data.HttpResponse;
import enkan.middleware.session.KeyValueStore;
import enkan.middleware.session.MemoryStore;

import javax.enterprise.context.Conversation;
import javax.inject.Inject;
import java.util.Objects;
import java.util.function.Function;

import static enkan.util.BeanBuilder.builder;
import static enkan.util.ThreadingUtils.some;

/**
 * Creates/Restores and conversation.
 *
 * @author kawasima
 */
@Middleware(name = "conversation")
public class ConversationMiddleware<NRES> extends AbstractWebMiddleware<HttpRequest, NRES> {
    private KeyValueStore store;
    private Function<HttpRequest, String> readTokenFunc;

    @Inject
    private HmacEncoder hmacEncoder;

    private static final Function<HttpRequest, String> DEFAULT_READ_TOKEN_FUNC = request ->
            some(request, HttpRequest::getFormParams,
                    p -> p.get("__conversation-token"),
                    Objects::toString).orElse(null);

    public ConversationMiddleware() {
        store = new MemoryStore();
        readTokenFunc = DEFAULT_READ_TOKEN_FUNC;
    }

    private boolean isGetRequest(HttpRequest request) {
        String method = request.getRequestMethod();
        return "GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method);
    }

    private ConversationToken parseToken(String token) {
        if (token == null) {
            return new ConversationToken(null, "invalid", "0");
        }
        String[] tokens = token.trim().split("\\$", 3);

        // If token contains two '$' characters, it's invalid.
        if (tokens.length != 3) {
            return new ConversationToken(null, "invalid", "0");
        }
        return new ConversationToken(tokens[0], tokens[1], tokens[2]);
    }

    @Override
    public HttpResponse handle(HttpRequest request, MiddlewareChain<HttpRequest, NRES, ?, ?> chain) {
        ConversationToken token = parseToken(readTokenFunc.apply(request));

        if (!isGetRequest(request) && !token.isValid()) {
            return builder(HttpResponse.of("Invalid conversation."))
                    .set(HttpResponse::setHeaders, Headers.of("Content-Type", "text/plain"))
                    .set(HttpResponse::setStatus, 403)
                    .build();
        }

        Conversation conversation;
        if (token.id == null) {
            conversation = new DefaultConversation();
        } else {
            conversation = new DefaultConversation(token.id);
            ConversationState state = (ConversationState) store.read(token.id);
            request.setConversationState(state);
        }
        request.setConversation(conversation);

        HttpResponse response = castToHttpResponse(chain.next(request));
        if (conversation.isTransient()) {
            if (conversation.getId() != null) {
                store.delete(conversation.getId());
            }
        } else if (response.getConversationState() != null) {
            store.write(conversation.getId(), response.getConversationState());
        }
        return response;
    }

    private class ConversationToken {
        private String id;
        private String hash;
        private long expires;

        ConversationToken(String id, long expires) {
            this.id = id;
            this.hash = hmacEncoder.encodeToHex(id + "$" + expires);
            this.expires = expires;
        }

        ConversationToken(String id, String hash, String expires) {
            this.id = id;
            this.hash = hash;
            try {
                this.expires = Long.parseLong(expires);
            } catch (Exception e) {
                this.expires = 0;
            }
        }

        boolean isValid() {
            return id != null && Objects.equals(hash, hmacEncoder.encodeToHex(id + "$" + expires))
                    && (expires < 0 || System.currentTimeMillis() > expires);
        }

        @Override
        public String toString() {
            return id + "$" + hash + "$" + expires;
        }
    }
}
