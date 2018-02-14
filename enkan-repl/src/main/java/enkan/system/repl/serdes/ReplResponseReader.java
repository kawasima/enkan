package enkan.system.repl.serdes;

import enkan.system.ReplResponse;
import org.fressian.Reader;
import org.fressian.handlers.ReadHandler;

import java.io.IOException;
import java.util.Set;

/**
 * A fressian reader for ReplRepsponse.
 *
 * @author kawasima
 */
public class ReplResponseReader implements ReadHandler {
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object read(Reader r, Object tag, int componentCount) throws IOException {
        ReplResponse response = new ReplResponse();
        response.setOut((String) r.readObject());
        response.setErr((String) r.readObject());
        response.getStatus().addAll((Set<ReplResponse.ResponseStatus>) r.readObject());
        return response;
    }
}
