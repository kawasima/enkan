import enkan.system.ReplResponse;
import enkan.system.ReplResponse.ResponseStatus;
import enkan.system.repl.serdes.Fressian;
import enkan.system.repl.serdes.ReplResponseReader;
import enkan.system.repl.serdes.ReplResponseWriter;
import enkan.system.repl.serdes.ResponseStatusReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FressianTest {
    private Fressian fressian;

    @BeforeEach
    public void setUp() {
        fressian = new Fressian();
        fressian.putReadHandler(ReplResponse.class, new ReplResponseReader());
        fressian.putReadHandler(ResponseStatus.class, new ResponseStatusReader());
        fressian.putWriteHandler(ReplResponse.class, new ReplResponseWriter());
        fressian.putWriteHandler(ResponseStatus.class, new ReplResponseWriter());
    }

    @Test
    public void outFieldRoundtrip() {
        ReplResponse response = ReplResponse.withOut("hello");
        byte[] bytes = fressian.write(response);
        ReplResponse deserialized = fressian.read(bytes, ReplResponse.class);

        assertThat(deserialized.getOut()).isEqualTo("hello");
        assertThat(deserialized.getErr()).isNull();
        assertThat(deserialized.getValue()).isNull();
    }

    @Test
    public void errFieldRoundtrip() {
        ReplResponse response = ReplResponse.withErr("something went wrong");
        byte[] bytes = fressian.write(response);
        ReplResponse deserialized = fressian.read(bytes, ReplResponse.class);

        assertThat(deserialized.getErr()).isEqualTo("something went wrong");
        assertThat(deserialized.getOut()).isNull();
        assertThat(deserialized.getStatus()).contains(ResponseStatus.ERROR);
    }

    @Test
    public void valueFieldRoundtrip() {
        ReplResponse response = new ReplResponse();
        response.setValue("42");
        byte[] bytes = fressian.write(response);
        ReplResponse deserialized = fressian.read(bytes, ReplResponse.class);

        assertThat(deserialized.getValue()).isEqualTo("42");
        assertThat(deserialized.getOut()).isNull();
        assertThat(deserialized.getErr()).isNull();
    }

    @Test
    public void doneStatusRoundtrip() {
        ReplResponse response = ReplResponse.withOut("result").done();
        byte[] bytes = fressian.write(response);
        ReplResponse deserialized = fressian.read(bytes, ReplResponse.class);

        assertThat(deserialized.getOut()).isEqualTo("result");
        assertThat(deserialized.getStatus()).contains(ResponseStatus.DONE);
    }

    @Test
    public void shutdownStatusRoundtrip() {
        ReplResponse response = ReplResponse.withOut("shutdown");
        response.getStatus().add(ResponseStatus.SHUTDOWN);
        byte[] bytes = fressian.write(response);
        ReplResponse deserialized = fressian.read(bytes, ReplResponse.class);

        assertThat(deserialized.getStatus()).contains(ResponseStatus.SHUTDOWN);
    }

    @Test
    public void allFieldsRoundtrip() {
        ReplResponse response = new ReplResponse();
        response.setOut("out text");
        response.setErr("err text");
        response.setValue("val text");
        response.getStatus().add(ResponseStatus.DONE);
        byte[] bytes = fressian.write(response);
        ReplResponse deserialized = fressian.read(bytes, ReplResponse.class);

        assertThat(deserialized.getOut()).isEqualTo("out text");
        assertThat(deserialized.getErr()).isEqualTo("err text");
        assertThat(deserialized.getValue()).isEqualTo("val text");
        assertThat(deserialized.getStatus()).contains(ResponseStatus.DONE);
    }
}
