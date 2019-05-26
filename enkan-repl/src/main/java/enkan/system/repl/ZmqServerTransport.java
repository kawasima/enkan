package enkan.system.repl;

import enkan.system.ReplResponse;
import enkan.system.Transport;
import enkan.system.repl.serdes.Fressian;
import enkan.system.repl.serdes.ReplResponseReader;
import enkan.system.repl.serdes.ReplResponseWriter;
import enkan.system.repl.serdes.ResponseStatusReader;
import org.zeromq.ZFrame;
import org.zeromq.ZMsg;

import static org.zeromq.ZMQ.*;

/**
 * Transport via socket.
 *
 * @author kawasima
 */
public class ZmqServerTransport implements Transport {
    private static final Fressian fressian;
    static {
        fressian = new Fressian();
        fressian.putReadHandler(ReplResponse.class, new ReplResponseReader());
        fressian.putReadHandler(ReplResponse.ResponseStatus.class, new ResponseStatusReader());
        fressian.putWriteHandler(ReplResponse.class, new ReplResponseWriter());
        fressian.putWriteHandler(ReplResponse.ResponseStatus.class, new ReplResponseWriter());
    }

    private boolean isClosed = false;
    private Socket socket;
    private ZFrame clientAddress;

    public ZmqServerTransport(Socket socket, ZFrame clientAddress) {
        this.socket = socket;
        this.clientAddress = clientAddress;
    }

    @Override
    public void send(ReplResponse response) {
        ZMsg msg = new ZMsg();
        msg.add(clientAddress.duplicate());
        msg.add(fressian.write(response));
        msg.send(socket, true);
    }

    @Override
    public String recv(long timeout) {
        ZMsg msg = ZMsg.recvMsg(socket);
        clientAddress = msg.pop();
        return msg.popString();
    }

    public void close() {
        isClosed = true;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public ZFrame getClientAddress() {
        return clientAddress;
    }
}
