package enkan.system.repl.client;

import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.reader.Candidate;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.util.List;

/**
 * A JLine completer implementation that communicates with a remote REPL server
 * to provide code completion suggestions.
 *
 * <p>
 * This class uses ZeroMQ socket communication to request and receive completion
 * candidates from a remote server. It sends the current buffer state and cursor position
 * to the remote server and receives back completion suggestions.
 * </p>
 *
 * @author kawasima
 */
public class RemoteCompleter implements Completer {
    /** The ZeroMQ socket used to communicate with the remote server. */
    private final ZMQ.Socket socket;

    public RemoteCompleter(ZMQ.Socket socket) {
        this.socket = socket;
        socket.setReceiveTimeOut(5000);
    }

    /**
     * Completes the current input line by requesting suggestions from the remote server.
     *
     * <p>
     * This method sends the current buffer and cursor position to the remote server
     * using ZeroMQ messaging. It then waits for the server's response, which contains
     * completion candidates. These candidates are then added to the provided candidates list.
     * </p>
     *
     * @param reader     the line reader
     * @param line       the parsed command line
     * @param candidates the list to which completion candidates are added
     */
    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        String buffer = line.line();
        int cursor = line.cursor();

        ZMsg msg = new ZMsg();
        msg.add(""); // delimiter
        msg.add(buffer);
        msg.add(Integer.toString(cursor));
        if (!msg.send(socket)) {
            return;
        }

        ZMsg response = ZMsg.recvMsg(socket);
        if (response == null) {
            return;
        }

        response.pop(); // delimiter
        while (!response.isEmpty()) {
            String suggestion = response.popString();
            candidates.add(new Candidate(suggestion));
        }
    }
}
