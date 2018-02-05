package enkan.system.repl.client;

import jline.console.completer.Completer;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;
import org.zeromq.ZPoller;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

public class RemoteCompleter implements Completer {
    private ZMQ.Socket socket;

    public RemoteCompleter(ZMQ.Socket socket) {
        this.socket = socket;
        socket.setReceiveTimeOut(5000);
    }
    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {
        ZMsg msg = new ZMsg();
        msg.add(buffer);
        msg.add(Integer.toString(cursor));
        msg.send(socket);

        ZMsg response = ZMsg.recvMsg(socket);
        while (response != null && !response.isEmpty()) {
            candidates.add(response.popString());
        }
        if (candidates.isEmpty()) return cursor;

        int dotPos = buffer.lastIndexOf('.');
        if (dotPos > 0) {
            return dotPos + 1;
        } else {
            return 0;
        }
    }
}
