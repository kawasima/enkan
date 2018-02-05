import enkan.system.ReplResponse;
import enkan.system.repl.serdes.Fressian;
import enkan.system.repl.serdes.ReplResponseReader;
import enkan.system.repl.serdes.ReplResponseWriter;
import enkan.system.repl.serdes.ResponseStatusReader;
import org.junit.jupiter.api.Test;

public class FressianTest {
    @Test
    public void test() {
        Fressian fressian = new Fressian();
        fressian.putReadHandler(ReplResponse.class, new ReplResponseReader());
        fressian.putReadHandler(ReplResponse.ResponseStatus.class, new ResponseStatusReader());
        fressian.putWriteHandler(ReplResponse.class, new ReplResponseWriter());
        fressian.putWriteHandler(ReplResponse.ResponseStatus.class, new ReplResponseWriter());

        ReplResponse response = ReplResponse.withOut("hoge").done();
        byte[] bytes = fressian.write(response);
        System.out.println(bytes.length);
        System.out.println(fressian.read(bytes, ReplResponse.class));
        ReplResponse deserialized = fressian.read(bytes, ReplResponse.class);
        System.out.println(deserialized.getStatus());

    }
}
