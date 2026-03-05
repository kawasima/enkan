package enkan.system.repl.websocket;

import enkan.system.ReplResponse;
import org.junit.jupiter.api.Test;

import static enkan.system.ReplResponse.ResponseStatus.*;
import static org.assertj.core.api.Assertions.assertThat;

class WebSocketServerTransportTest {

    @Test
    void toJson_outOnly() {
        ReplResponse res = ReplResponse.withOut("hello");
        String json = WebSocketServerTransport.toJson(res);
        assertThat(json).isEqualTo("{\"out\":\"hello\"}");
    }

    @Test
    void toJson_errOnly() {
        ReplResponse res = ReplResponse.withErr("fail");
        String json = WebSocketServerTransport.toJson(res);
        assertThat(json).contains("\"err\":\"fail\"");
    }

    @Test
    void toJson_withDoneStatus() {
        ReplResponse res = ReplResponse.withOut("ok");
        res.getStatus().add(DONE);
        String json = WebSocketServerTransport.toJson(res);
        assertThat(json).contains("\"status\":[\"DONE\"]");
    }

    @Test
    void toJson_escapesSpecialCharacters() {
        ReplResponse res = ReplResponse.withOut("line1\nline2\t\"quoted\"\\back");
        String json = WebSocketServerTransport.toJson(res);
        assertThat(json).contains("line1\\nline2\\t\\\"quoted\\\"\\\\back");
    }

    @Test
    void toJson_escapesControlCharacters() {
        ReplResponse res = ReplResponse.withOut("before\u0001after");
        String json = WebSocketServerTransport.toJson(res);
        assertThat(json).contains("before\\u0001after");
    }

    @Test
    void recv_returnsEnqueuedMessage() throws Exception {
        // WebSocketServerTransport needs a session, but we only test enqueue/recv
        // Use a mock-like approach: create transport with null session (recv doesn't use it)
        WebSocketServerTransport transport = new WebSocketServerTransport(null);
        transport.enqueue("test message");
        String result = transport.recv(1000);
        assertThat(result).isEqualTo("test message");
    }

    @Test
    void recv_returnsNullOnTimeout() {
        WebSocketServerTransport transport = new WebSocketServerTransport(null);
        String result = transport.recv(100);
        assertThat(result).isNull();
    }
}
