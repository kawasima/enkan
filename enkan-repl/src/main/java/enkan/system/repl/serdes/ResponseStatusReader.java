package enkan.system.repl.serdes;

import enkan.system.ReplResponse.ResponseStatus;
import org.fressian.Reader;
import org.fressian.handlers.ReadHandler;

import java.io.IOException;

public class ResponseStatusReader implements ReadHandler {
    @Override
    public Object read(Reader r, Object tag, int componentCount) throws IOException {
        return ResponseStatus.valueOf((String) r.readObject());
    }
}
