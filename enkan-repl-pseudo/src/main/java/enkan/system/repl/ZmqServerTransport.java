package enkan.system.repl;

import enkan.exception.FalteringEnvironmentException;
import enkan.system.ReplResponse;
import enkan.system.Transport;
import org.msgpack.MessagePack;
import org.zeromq.ZFrame;
import org.zeromq.ZMsg;

import java.io.IOException;

import static org.zeromq.ZMQ.*;

/**
 * Transport via socket.
 *
 * @author kawasima
 */
public class ZmqServerTransport implements Transport {
    private static MessagePack msgpack;
    static {
        msgpack = new MessagePack();
        msgpack.register(ReplResponse.ResponseStatus.class);
        msgpack.register(ReplResponse.class);
    }

    private Socket socket;
    private ZFrame clientAddress;

    public ZmqServerTransport(Socket socket, ZFrame clientAddress) {
        this.socket = socket;
        this.clientAddress = clientAddress;
    }

    @Override
    public void send(ReplResponse response) {
        try {
            ZMsg msg = new ZMsg();
            msg.add(clientAddress.duplicate());
            msg.add(msgpack.write(response));
            msg.send(socket, true);
        } catch (IOException ex) {
            throw new FalteringEnvironmentException(ex);
        }
    }

    @Override
    public String recv(long timeout) {
        ZMsg msg = ZMsg.recvMsg(socket);
        clientAddress = msg.pop();
        return msg.popString();
    }

    public void close() throws IOException {
        // Do nothing
    }

}
