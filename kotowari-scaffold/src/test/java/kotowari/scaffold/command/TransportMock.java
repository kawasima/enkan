package kotowari.scaffold.command;

import enkan.system.ReplResponse;
import enkan.system.Transport;

/**
 * @author kawasima
 */
public class TransportMock implements Transport {
    @Override
    public void send(ReplResponse response) {

    }

    @Override
    public String recv(long timeout) {
        return null;
    }
}
