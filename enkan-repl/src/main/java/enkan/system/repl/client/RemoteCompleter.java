package enkan.system.repl.client;

import jline.console.completer.Completer;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.util.List;

public class RemoteCompleter implements Completer {
    private final ZMQ.Socket socket;

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

        int delimiterPos = Math.max(buffer.lastIndexOf(' '), buffer.lastIndexOf('.'));
        if (delimiterPos > 0) {
            return delimiterPos + 1;
        } else {
            return 0;
        }
    }
}
