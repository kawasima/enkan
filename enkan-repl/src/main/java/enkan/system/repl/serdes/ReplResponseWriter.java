package enkan.system.repl.serdes;

import enkan.system.ReplResponse;
import enkan.system.ReplResponse.ResponseStatus;
import org.fressian.Writer;
import org.fressian.handlers.WriteHandler;

import java.io.IOException;
import java.util.Collections;

public class ReplResponseWriter implements WriteHandler {
    @Override
    public void write(Writer w, Object obj) throws IOException {
        if (obj instanceof ReplResponse) {
            w.writeTag("ReplResponse", 3);
            ReplResponse replResponse = (ReplResponse) obj;
            w.writeString(replResponse.getOut());
            w.writeString(replResponse.getErr());
            w.writeObject(Collections.unmodifiableSet(replResponse.getStatus()));
        } else if (obj instanceof ReplResponse.ResponseStatus) {
            w.writeTag("ResponseStatus", 1);
            ResponseStatus status = (ResponseStatus) obj;
            w.writeString(status.name());
        }
    }
}
