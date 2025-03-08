package enkan.system.repl.client;

import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.reader.Candidate;
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
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        String buffer = line.line();
        int cursor = line.cursor();

        System.out.println("Completing: buffer='" + buffer + "', cursor=" + cursor);

        ZMsg msg = new ZMsg();
        msg.add(""); // delimiter
        msg.add(buffer);
        msg.add(Integer.toString(cursor));
        boolean sent = msg.send(socket);
        System.out.println("Sent completion request: " + sent);

        ZMsg response = ZMsg.recvMsg(socket);
        if (response == null) {
            System.err.println("No response received from completion server");
            return;
        }
        System.out.println("Received completion response with " + response.size() + " frames");

        response.pop(); // delimiter
        while (!response.isEmpty()) {
            String suggestion = response.popString();
            System.out.println("Adding completion candidate: " + suggestion);
            candidates.add(new Candidate(suggestion));
        }
    }
}
