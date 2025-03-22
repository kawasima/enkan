package enkan.system.repl.serdes;

import enkan.system.ReplResponse;
import enkan.system.ReplResponse.ResponseStatus;
import org.fressian.Writer;
import org.fressian.handlers.WriteHandler;

import java.io.IOException;
import java.util.Collections;

/**
 * A write handler for ReplResponse.
 * @author kawasima
 */
public class ReplResponseWriter implements WriteHandler {
    @Override
    public void write(Writer w, Object obj) throws IOException {
        if (obj instanceof ReplResponse replResponse) {
            w.writeTag("ReplResponse", 3);
            w.writeString(replResponse.getOut());
            w.writeString(replResponse.getErr());
            w.writeObject(Collections.unmodifiableSet(replResponse.getStatus()));
        } else if (obj instanceof ResponseStatus status) {
            w.writeTag("ResponseStatus", 1);
            w.writeString(status.name());
        }
    }
}
